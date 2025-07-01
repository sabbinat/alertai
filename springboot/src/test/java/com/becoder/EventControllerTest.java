package com.becoder;

import com.sbact1.controller.EventController;
import com.sbact1.dto.EventDto;
import com.sbact1.model.*;
import com.sbact1.repository.*;
import com.sbact1.service.EmailService;
import com.sbact1.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock private EventService eventService;
    @Mock private EventRepository eventRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private EmailService emailService;
    @Mock private CategoryRepository categoryRepository;

    @Mock private Principal principal;
    @Mock private Model model;
    @Mock private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(principal.getName()).thenReturn("user@mail.com");
    }

    @Test
    void saveEvent_withCustomCategory() throws Exception {
        EventDto eventDto = mock(EventDto.class);
        when(eventDto.getImageFile()).thenReturn(null);
        Category category = new Category();
        category.setId(10L);
        category.setName("CustomCat");
        when(categoryRepository.findByName("CustomCat")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Event event = new Event();
        event.setCategory(category);
        event.setName("EventName");
        event.setDescription("Desc");
        event.setStartDate(LocalDate.now());
        when(eventService.saveEvent(any(), any(), anyString(), eq(10L))).thenReturn(event);

        User user = new User();
        user.setEmail("user2@mail.com");
        user.setName("User2");
        when(userRepository.findByNotificacionesContaining(category)).thenReturn(List.of(user));

        String result = eventController.saveEvent(eventDto, principal, 9L, "CustomCat");
        assertEquals("redirect:/user/home", result);
        verify(emailService).enviarCorreo(
                eq("user2@mail.com"),
                contains("Nuevo evento"),
                contains("Hola User2")
        );
    }

    @Test
    void deleteEvent_asAdmin() {
        User admin = new User();
        admin.setRole("ROLE_ADMIN");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));

        String result = eventController.deleteEvent(1L, principal, redirectAttributes);
        assertEquals("redirect:/admin/allEvents", result);
        verify(redirectAttributes).addFlashAttribute(eq("successMessage"), any());
    }

    @Test
    void deleteEvent_asUser() {
        User user = new User();
        user.setRole("ROLE_USER");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        String result = eventController.deleteEvent(1L, principal, redirectAttributes);
        assertEquals("redirect:/user/profile", result);
    }

    @Test
    void reportEvent_notLoggedIn() {
        RedirectAttributes ra = mock(RedirectAttributes.class);
        String result = eventController.reportEvent(1L, ReportReason.SPAM, null, ra);
        assertEquals("redirect:/event/1", result);
        verify(ra).addFlashAttribute(eq("error"), any());
    }

    @Test
    void reportEvent_duplicateReport() {
        RedirectAttributes ra = mock(RedirectAttributes.class);
        Event event = new Event();
        event.setId(1L);
        event.setStatus(EventStatus.ACTIVO);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(reportRepository.existsByEventAndUser(event, user)).thenReturn(true);

        String result = eventController.reportEvent(1L, ReportReason.SPAM, principal, ra);
        assertEquals("redirect:/event/1", result);
        verify(ra).addFlashAttribute(eq("error"), contains("Ya has denunciado"));
    }

    @Test
    void reportEvent_firstReport() {
        RedirectAttributes ra = mock(RedirectAttributes.class);
        Event event = new Event();
        event.setId(1L);
        event.setStatus(EventStatus.ACTIVO);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(reportRepository.existsByEventAndUser(event, user)).thenReturn(false);
        when(reportRepository.countByEventId(1L)).thenReturn(1);

        String result = eventController.reportEvent(1L, ReportReason.SPAM, principal, ra);
        assertEquals("redirect:/event/1", result);
        verify(reportRepository).save(any(Report.class));
        verify(eventRepository).save(event);
        verify(ra).addFlashAttribute(eq("success"), any());
        assertEquals(EventStatus.DENUNCIADO, event.getStatus());
    }

    @Test
    void reportEvent_revisionStatus() {
        RedirectAttributes ra = mock(RedirectAttributes.class);
        Event event = new Event();
        event.setId(1L);
        event.setStatus(EventStatus.ACTIVO);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(reportRepository.existsByEventAndUser(event, user)).thenReturn(false);
        when(reportRepository.countByEventId(1L)).thenReturn(3);

        String result = eventController.reportEvent(1L, ReportReason.SPAM, principal, ra);
        assertEquals("redirect:/event/1", result);
        verify(eventRepository).save(event);
        assertEquals(EventStatus.REVISION, event.getStatus());
    }

    @Test
    void changeStatusEvent() {
        Event event = new Event();
        event.setStatus(EventStatus.ACTIVO);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        String result = eventController.cambiarEstadoEvento(1L, EventStatus.INACTIVO, null, redirectAttributes);
        assertEquals("redirect:/admin/allEvents", result);
        verify(eventRepository).save(event);
        verify(redirectAttributes).addFlashAttribute(eq("success"), any());
        assertEquals(EventStatus.INACTIVO, event.getStatus());
    }


    @Test
    void postCommnet_newComment() {
        Event event = new Event();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        String result = eventController.publicarComentario(1L, "content", null, principal);
        assertEquals("redirect:/event/1", result);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void postComment_replyComment() {
        Event event = new Event();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        User user = new User();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        Comment parent = new Comment();
        when(commentRepository.findById(2L)).thenReturn(Optional.of(parent));

        String result = eventController.publicarComentario(1L, "content", 2L, principal);
        assertEquals("redirect:/event/1", result);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteComment_authorCanDelete() {
        Comment comment = new Comment();
        User user = new User();
        user.setId(1);
        comment.setUser(user);
        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        String result = eventController.eliminarComentario(1L, 2L, principal);
        assertEquals("redirect:/event/1", result);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_notAuthorThrows() {
        Comment comment = new Comment();
        User user = new User();
        user.setId(1);
        comment.setUser(user);
        User another = new User();
        another.setId(2);
        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(another));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> eventController.eliminarComentario(1L, 2L, principal));
    }
}