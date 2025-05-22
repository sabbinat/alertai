package com.sbact1.controller;

import java.security.Principal;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.sbact1.model.Comment;
import com.sbact1.model.Event;
import com.sbact1.model.User;
import com.sbact1.repository.CommentRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;

@Controller
@RequestMapping("/event")
public class CommentController {
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Método para publicar un comentario o una respuesta a un comentario en un evento específico.
     * 
     * @param id ID del evento al que se asocia el comentario.
     * @param content Contenido del comentario.
     * @param parentId (opcional) ID del comentario padre si se trata de una respuesta.
     * @param principal Objeto que representa al usuario autenticado.
     * @return Redirige a la vista del evento después de guardar el comentario.
     */
    @PostMapping("/{id}/comentario")
    public String publicarComentario(@PathVariable Long id, @RequestParam String content, @RequestParam(required = false) Long parentId,
                                     Principal principal) {

        // Buscar el evento al que se le agregará el comentario
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        // Obtiene el usuario logueado
        User user = userRepository.findByEmail(principal.getName());

        // Crea un nuevo objeto Comment y asigna el contenido, evento y usuario
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setEvent(event);
        comment.setUser(user);

        // Si es una respuesta a otro comentario, busca el comentario padre y lo asignar
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario padre no encontrado"));
            comment.setParent(parentComment);
        }

        // Guardar el comentario en la base de datos
        commentRepository.save(comment);

        return "redirect:/event/" + id;
    }

    //Método para eliminar un comentario de un evento. - Solo el autor del comentario puede eliminarlo.
    @PostMapping("/{eventId}/comentario/{commentId}/delete")
    public String eliminarComentario(@PathVariable Long eventId, 
                                     @PathVariable Long commentId, 
                                     Principal principal) {
        // Busca el comentario a eliminar
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        // Obtener el usuario logueado
        User user = userRepository.findByEmail(principal.getName());

        // Solo el autor del comentario puede eliminarlo
        if (!Objects.equals(comment.getUser().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar este comentario");
        }

        commentRepository.delete(comment);
        return "redirect:/event/" + eventId;
    }
}
