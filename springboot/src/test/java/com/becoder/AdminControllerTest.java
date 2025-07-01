package com.becoder;
import com.sbact1.model.User;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.UserService;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.sbact1.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sbact1.controller.AdminController;
import com.sbact1.model.Event;
import com.sbact1.model.EventStatus;
import com.sbact1.model.Report;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.ReportRepository;

import java.io.UnsupportedEncodingException;
import java.util.List;


class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deleteUser_userNotFound() {
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String result = adminController.deleteUser(userId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(eq("errorEliminar"), eq("Ususario no encontrado"));
        assertEquals("redirect:/admin/users", result);
        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    void deleteUser_userFound() {
        Integer userId = 2;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String result = adminController.deleteUser(userId, redirectAttributes);

        verify(userService).deleteUser(userId);
        verify(redirectAttributes).addFlashAttribute(eq("sucessoEliminar"), eq("Usuario eliminado exitosamente!"));
        assertEquals("redirect:/admin/users", result);
    }

    @Test
    void changeRole_userExist() {
        Integer userId = 10;
        String newRole = "ROLE_ADMIN";
        User user = new User();
        user.setRole("ROLE_USER");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = adminController.changeRole(userId, newRole);

        assertEquals("redirect:/admin/users", result);
        assertEquals(newRole, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void changeRole_userDosentExist() {
        Integer userId = 20;
        String newRole = "ROLE_ADMIN";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        String result = adminController.changeRole(userId, newRole);

        assertEquals("redirect:/admin/users", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_userNotFound() throws UnsupportedEncodingException, MessagingException {
        int userId = 5;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String result = adminController.resetPassword(userId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Usuario no encontrado."));
        assertEquals("redirect:/admin/users", result);
        verify(userRepository, never()).save(any());
        verify(emailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_userFound() throws Exception {
        int userId = 6;
        User user = new User();
        user.setName("Juan");
        user.setEmail("juan@email.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String result = adminController.resetPassword(userId, redirectAttributes);

        verify(userRepository).save(user);
        verify(emailService).enviarCorreo(eq("juan@email.com"), eq("Contraseña restablecida"), contains("Temporal123@"));
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Contraseña restablecida y correo enviado al usuario."));
        assertEquals("redirect:/admin/users", result);
    }

    @Test
    void resetPassword_userFound_failureSendEmail() throws Exception {
        int userId = 7;
        User user = new User();
        user.setName("Ana");
        user.setEmail("ana@email.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Email error")).when(emailService).enviarCorreo(anyString(), anyString(), anyString());
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String result = adminController.resetPassword(userId, redirectAttributes);

        verify(userRepository).save(user);
        verify(emailService).enviarCorreo(eq("ana@email.com"), eq("Contraseña restablecida"), contains("Temporal123@"));
        verify(redirectAttributes).addFlashAttribute(eq("warning"), contains("Contraseña restablecida, pero no se pudo enviar el correo."));
        assertEquals("redirect:/admin/users", result);
    }

    @Test
    void approveEventAndMarkReportLikeReviwed() {
        Long eventId = 100L;
        Event event = new Event();
        event.setStatus(EventStatus.REVISION);

        Report report1 = new Report();
        report1.setReviewed(false);
        Report report2 = new Report();
        report2.setReviewed(false);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(reportRepository.findByEventId(eventId)).thenReturn(List.of(report1, report2));
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);

        String result = adminController.approveEvent(eventId, redirectAttributes);

        assertEquals(EventStatus.ACTIVO, event.getStatus());
        verify(eventRepository).save(event);
        assertTrue(report1.isReviewed());
        assertTrue(report2.isReviewed());
        verify(reportRepository).saveAll(List.of(report1, report2));
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("El evento fue aprobado exitosamente"));
        assertEquals("redirect:/admin/reports", result);
    }

}