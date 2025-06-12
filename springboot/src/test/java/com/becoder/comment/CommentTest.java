package com.becoder.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.sbact1.controller.CommentController;
import com.sbact1.model.Comment;
import com.sbact1.model.Event;
import com.sbact1.model.User;
import com.sbact1.repository.CommentRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CommentTest {

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private Principal principal;

    @Test
    void publicarComentario_eventoYusuarioExisten_guardarComentario() {
        Long eventId = 1L;
        String content = "Buen evento!";
        String userEmail = "user@example.com";

        Event event = new Event();
        event.setId(eventId);

        User user = new User();
        user.setId(10);
        user.setEmail(userEmail);

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        Mockito.when(principal.getName()).thenReturn(userEmail);
        Mockito.when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        String redirect = commentController.publicarComentario(eventId, content, null, principal);

        assertEquals("redirect:/event/" + eventId, redirect);

        Mockito.verify(commentRepository).save(argThat(comment ->
            comment.getContent().equals(content) &&
            comment.getEvent().equals(event) &&
            comment.getUser().equals(user) &&
            comment.getParent() == null
        ));
    }

    @Test
    void publicarRespuesta_comentarioPadreExistente() {
        Long eventId = 1L;
        Long parentId = 100L;
        String content = "Respuesta al comentario";
        String userEmail = "user@example.com";

        Event event = new Event();
        event.setId(eventId);

        User user = new User();
        user.setId(10);
        user.setEmail(userEmail);

        Comment parentComment = new Comment();
        parentComment.setId(parentId);

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        Mockito.when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
        Mockito.when(principal.getName()).thenReturn(userEmail);
        Mockito.when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));

        String redirect = commentController.publicarComentario(eventId, content, parentId, principal);

        assertEquals("redirect:/event/" + eventId, redirect);

        Mockito.verify(commentRepository).save(argThat(comment ->
            comment.getContent().equals(content) &&
            comment.getEvent().equals(event) &&
            comment.getUser().equals(user) &&
            comment.getParent().equals(parentComment)
        ));
    }

    @Test
    void publicarComentario_usuarioNoExiste_lanza401() {
        Long eventId = 1L;
        String userEmail = "user@example.com";

        Event event = new Event();
        event.setId(eventId);

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());
        Mockito.when(principal.getName()).thenReturn(userEmail);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            commentController.publicarComentario(eventId, "texto", null, principal)
        );
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Usuario no encontrado"));
    }

    @Test
    void eliminarComentario_autorElimina_exito() {
        Long eventId = 1L;
        Long commentId = 10L;
        String userEmail = "user@example.com";

        User user = new User();
        user.setId(10);
        user.setEmail(userEmail);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(user);

        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        Mockito.when(principal.getName()).thenReturn(userEmail);

        String redirect = commentController.eliminarComentario(eventId, commentId, principal);

        assertEquals("redirect:/event/" + eventId, redirect);
        Mockito.verify(commentRepository).delete(comment);
    }

    @Test
    void eliminarComentario_otroUsuario_lanza403() {
        Long eventId = 1L;
        Long commentId = 10L;
        String userEmail = "user@example.com";

        User user = new User();
        user.setId(10);
        user.setEmail(userEmail);

        User otroUsuario = new User();
        otroUsuario.setId(99);
        otroUsuario.setEmail("otro@correo.com");

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUser(otroUsuario);

        Mockito.when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        Mockito.when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        Mockito.when(principal.getName()).thenReturn(userEmail);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            commentController.eliminarComentario(eventId, commentId, principal)
        );
        assertEquals(403, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("No puedes eliminar este comentario"));
    }

}

