package com.becoder;
import com.sbact1.controller.UserController;
import com.sbact1.model.*;
import com.sbact1.repository.*;
import com.sbact1.service.*;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private EventRepository eventRepository;
    @Mock private SettingService settingService;
    @Mock private TokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private UserService userService;
    @Mock private CommentRepository commentRepository;
    @Mock private ReportRepository reportRepository;

    @Mock private Model model;
    @Mock private Principal principal;
    @Mock private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void updateProfile_AdminRedirect() {
        User updatedUser = new User();
        User user = new User();
        user.setRole("ROLE_ADMIN");
        when(principal.getName()).thenReturn("admin@mail.com");
        when(userRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(user));

        String result = userController.updateProfile(updatedUser, null, false, principal);

        assertEquals("redirect:/admin/home", result);
    }

    @Test
    void updateProfile_UserRedirect() {
        User updatedUser = new User();
        User user = new User();
        user.setRole("ROLE_USER");
        when(principal.getName()).thenReturn("user@mail.com");
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

        String result = userController.updateProfile(updatedUser, null, false, principal);

        assertEquals("redirect:/user/profile", result);
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userService).deleteUser(1);
        String result = userController.deleteUser(1, redirectAttributes);
        assertEquals("redirect:/signin", result);
        verify(redirectAttributes).addFlashAttribute(eq("sucessoEliminar"), any());
    }
    
    @Test
    void requestDeletion_Success() throws UnsupportedEncodingException, MessagingException {
        User user = new User();
        user.setEmail("test@mail.com");
        when(principal.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenReturn(new Token());

        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        String result = userController.requestDeletion(principal, redirectAttributes);

        assertEquals("redirect:/user/profile", result);
        verify(redirectAttributes).addFlashAttribute(eq("successEliminar"), any());
    }

    @Test
    void requestDeletion_EmailException() throws UnsupportedEncodingException, MessagingException {
        User user = new User();
        user.setEmail("test@mail.com");
        when(principal.getName()).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenReturn(new Token());

        doThrow(new RuntimeException()).when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        String result = userController.requestDeletion(principal, redirectAttributes);

        assertEquals("redirect:/user/profile", result);
        verify(redirectAttributes).addFlashAttribute(eq("errorEliminar"), any());
    }

    @Test
    void confirmDeletion_TokenExpired() {
        Token token = new Token();
        token.setExpiryDate(LocalDateTime.now().minusHours(2));
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        String result = userController.confirmDeletion("token", redirectAttributes);

        assertEquals("redirect:/signin", result);
        verify(redirectAttributes).addFlashAttribute(eq("errorEliminar"), any());
    }

    @Test
    void confirmDeletion_Success() {
        User user = new User();
        user.setId(1);
        Token token = new Token();
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUser(user);
        when(tokenRepository.findByToken("token")).thenReturn(Optional.of(token));

        String result = userController.confirmDeletion("token", redirectAttributes);

        assertEquals("redirect:/signin", result);
        verify(tokenRepository).delete(token);
        verify(userService).deleteUser(user.getId());
        verify(redirectAttributes).addFlashAttribute(eq("sucessoEliminar"), any());
    }

    @Test
    void changePassword_PasswordsDontMatch() {
        when(principal.getName()).thenReturn("user@mail.com");
        String result = userController.changePassword("old", "new", "different", principal, redirectAttributes);
        assertEquals("redirect:/user/profile", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        when(principal.getName()).thenReturn("user@mail.com");
        when(userService.cambiarPassword("user@mail.com", "old", "new")).thenReturn(false);

        String result = userController.changePassword("old", "new", "new", principal, redirectAttributes);

        assertEquals("redirect:/user/profile", result);
        verify(redirectAttributes).addFlashAttribute(eq("error"), any());
    }

    @Test
    void changePassword_Success() {
        when(principal.getName()).thenReturn("user@mail.com");
        when(userService.cambiarPassword("user@mail.com", "old", "new")).thenReturn(true);

        String result = userController.changePassword("old", "new", "new", principal, redirectAttributes);

        assertEquals("redirect:/user/profile", result);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
    }

}