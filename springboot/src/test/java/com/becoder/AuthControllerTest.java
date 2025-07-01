package com.becoder;
import com.sbact1.controller.AuthController;
import com.sbact1.model.Token;
import com.sbact1.model.User;
import com.sbact1.repository.TokenRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.EmailService;
import com.sbact1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserService userService;

    @Mock
    private BindingResult bindingResult;
    @Mock
    private HttpSession session;
    @Mock
    private Model model;
    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_EmailExists() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        String view = authController.registerUser(user, bindingResult, session);

        verify(bindingResult).rejectValue(eq("email"), any(), any());
        assertEquals("auth/register", view);
    }

    @Test
    void registerUser_HasErrors() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = authController.registerUser(user, bindingResult, session);

        assertEquals("auth/register", view);
    }

    @Test
    void registerUser_Success() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        String view = authController.registerUser(user, bindingResult, session);

        verify(tokenRepository).save(any(Token.class));
        verify(emailService).enviarCorreo(eq("test@example.com"), anyString(), contains("http://localhost:8080/confirmar?token="));
        verify(session).setAttribute(eq("msg"), contains("Registrado con éxito"));
        assertEquals("auth/successful_registration", view);
    }

    @Test
    void registerUser_EmailSendFails() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("fail")).when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        String view = authController.registerUser(user, bindingResult, session);

        verify(session).setAttribute(eq("msg"), contains("Hubo un problema"));
        assertEquals("auth/register", view);
    }

    @Test
    void confirmAccount_Success() {
        User user = new User();
        Token token = new Token();
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setPurpose("verify");
        token.setUser(user);
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        String view = authController.confirmAccount("token");

        assertTrue(user.isEnabled());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
        assertEquals("auth/verificado_exitoso", view);
    }
   
    @Test
    void processForgotPassword_UserNotFound() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String view = authController.processForgotPassword("nope@example.com", redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("No existe una cuenta"));
        assertEquals("redirect:/forgot-password", view);
    }

    @Test
    void processForgotPassword_EmailSendFails() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setName("Test");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("fail")).when(emailService).enviarCorreo(anyString(), anyString(), anyString());
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String view = authController.processForgotPassword("test@example.com", redirectAttributes);

        verify(tokenRepository).deleteByUserId(1);
        verify(tokenRepository).save(any(Token.class));
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("No se pudo enviar el correo"));
        assertEquals("redirect:/forgot-password", view);
    }

    @Test
    void processForgotPassword_Success() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setName("Test");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String view = authController.processForgotPassword("test@example.com", redirectAttributes);

        verify(tokenRepository).deleteByUserId(1);
        verify(tokenRepository).save(any(Token.class));
        verify(emailService).enviarCorreo(eq("test@example.com"), anyString(), contains("reset-password?token="));
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Te enviamos un correo"));
        assertEquals("redirect:/forgot-password", view);
    }

    @Test
    void showResetPasswordForm_Success() {
        Token token = new Token();
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        token.setPurpose("reset");
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        String view = authController.showResetPasswordForm("token", model);
        verify(model).addAttribute(eq("token"), eq("token"));
        assertEquals("auth/reset-password", view);
    }

    @Test
    void processResetPassword_Success() {
        User user = new User();
        Token token = new Token();
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        token.setPurpose("reset");
        token.setUser(user);
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedpass");
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String view = authController.processResetPassword("token", "newpass", redirectAttributes);

        assertEquals("encodedpass", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Contraseña restablecida"));
        assertEquals("redirect:/signin", view);
    }


}