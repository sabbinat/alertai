package com.sbact1.controller;

import com.sbact1.dto.EventDto;
import com.sbact1.model.*;
import com.sbact1.repository.*;
import com.sbact1.service.EmailService;
import com.sbact1.service.EventService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Controlador Spring MVC para la gestión de eventos en la aplicación.
 * 
 * Proporciona endpoints para:
 * 
 *   Crear, editar, actualizar y eliminar eventos.</li>
 *   Listar todos los eventos con filtros por nombre, categoría y mes.
 *   Mostrar detalles de un evento específico, incluyendo comentarios y denuncias.
 *   Reportar eventos por motivos específicos, cambiando su estado según la cantidad de reportes.
 *   Publicar y eliminar comentarios o respuestas en eventos (solo el autor puede eliminar).
 *   Cambiar el estado de un evento (activo, denunciado, en revisión, etc.).
 *   Notificar por correo a usuarios interesados cuando se crea un evento de su interés.
 * 
 * 
 * Seguridad:
 * 
 *   Requiere autenticación para operaciones sensibles (crear, editar, eliminar, reportar, comentar).
 *   Valida que solo el autor pueda eliminar sus propios comentarios.
 *   Permite a administradores gestionar el estado de los eventos.
 * 
 */
@Controller
@RequestMapping("/event")
public class EventController {

    @Autowired private EventService eventService;
    @Autowired private EventRepository eventRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private EmailService emailService; 
    @Autowired private CategoryRepository categoryRepository;

    // Método que guarda los eventos creados
    @PostMapping("/save")
    public String saveEvent(@ModelAttribute EventDto eventDto, Principal principal, @RequestParam("categoryId") Long categoryId,
                            @RequestParam(value = "customCategory", required = false) String customCategory,
                            RedirectAttributes msg) throws Exception {

        try{
            if (categoryId == 9 && customCategory != null && !customCategory.trim().isEmpty()) { // categoría "otro"
                Optional<Category> catOpt = categoryRepository.findByName(customCategory.trim());
                Category customCat;
                if (catOpt.isPresent()) {
                    customCat = catOpt.get();
                } else {
                    customCat = new Category();
                    customCat.setName(customCategory.trim());
                    customCat = categoryRepository.save(customCat);
                }
                categoryId = customCat.getId();
            }

            Event evento = eventService.saveEvent(eventDto, eventDto.getImageFile(), principal.getName(), categoryId);

            List<User> interesados = userRepository.findByNotificacionesContaining(evento.getCategory());
            for (User user : interesados) {
                String subject = "Nuevo evento en tu categoría favorita: " + evento.getCategory().getName();

                String content = """
                    <div style="font-family: Arial, sans-serif; color: #333;">
                        <p>Hola <strong>%s</strong>,</p>

                        <p>¡Tenemos una novedad que podría interesarte! Se ha publicado un nuevo evento relacionado con tu categoría favorita:</p>

                        <ul>
                            <li><strong>Nombre:</strong> %s</li>
                            <li><strong>Descripción:</strong> %s</li>
                            <li><strong>Fecha:</strong> %s</li>
                            <li><strong>Categoría:</strong> %s</li>
                        </ul>

                        <p>Te invitamos a ingresar al sitio para conocer todos los detalles..</p>

                        <p>¡Gracias por ser parte de nuestra comunidad!<br>
                        <em>El equipo de Star Software</em></p>
                    </div>
                    """.formatted(
                        user.getName(),
                        evento.getName(),
                        evento.getDescription(),
                        evento.getStartDate().toString(),
                        evento.getCategory().getName()
                    );

                emailService.enviarCorreo(user.getEmail(), subject, content);
            }

            msg.addFlashAttribute("success", "¡Evento creado exitosamente!");
            return "redirect:/event/" + evento.getId();
            
        } catch (Exception e) {
            msg.addFlashAttribute("error", "Ocurrió un error al crear el evento.");
            return "redirect:/user/home"; 
        }

    }


    // Método que muestra todos los eventos agrupados por categoría
    @GetMapping("/allEvents")
    public String listarTodosLosEventos(@RequestParam(value = "name", required = false) String name, 
                                        @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                        @RequestParam(value = "mes", required = false) Integer mes,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        Model model,
                                        Principal principal) {

        Pageable pageable = PageRequest.of(page, 8);
        Page<Event> eventos = eventService.buscarEventos(name, categoriaId, mes, pageable);

        model.addAttribute("eventos", eventos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventos.getTotalPages());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedName", name);
        model.addAttribute("selectedCategoriaId", categoriaId);
        model.addAttribute("selectedMonth", mes);

        // Obtiene el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }

        return "event/allEvents";
    }


    // Muestra los detalles de un evento específico
    @GetMapping("/{id}")
    public String showEvent(@PathVariable Long id, Model model, Principal principal) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("comments", commentRepository.findByEventId(id));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));
        String formattedDate = event.getStartDate().format(formatter);
        model.addAttribute("formattedDate", formattedDate);

        // Obtener el usuario autenticado de forma segura
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        } else {
            model.addAttribute("user", null);
        }

        model.addAttribute("reportReasons", ReportReason.values());

        return "event/event";
    }

    
    // Actualiza un evento existente
    @GetMapping("/edit/{id}")
    public String showEditEventForm(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id); 
        model.addAttribute("event", event);
        model.addAttribute("categories", categoryRepository.findAll());
        return "event/edit_event"; 
    }
    
    @PostMapping("/updateEvent")
    public String updateEvent(@ModelAttribute EventDto eventDto,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            @RequestParam(value = "redirectTo", required = false) String redirectTo,
                            @RequestParam("categoryId") Long categoryId,
                            @RequestParam(value = "customCategory", required = false) String customCategory,
                            Principal principal, RedirectAttributes msg) throws Exception {
        try{                       
            if (categoryId == 9 && customCategory != null && !customCategory.trim().isEmpty()) {
                Optional<Category> catOpt = categoryRepository.findByName(customCategory.trim());
                Category customCat;
                if (catOpt.isPresent()) {
                    customCat = catOpt.get();
                } else {
                    customCat = new Category();
                    customCat.setName(customCategory.trim());
                    customCat = categoryRepository.save(customCat);
                }
                categoryId = customCat.getId();
            }

            eventService.updateEvent(eventDto, imageFile, principal.getName());

            if (redirectTo != null && !redirectTo.isBlank()) {
                return "redirect:" + redirectTo;
            }

            msg.addFlashAttribute("success", "¡Evento actualizado con éxito!");
            return "redirect:/event/" + eventDto.getId();
        } catch (Exception e){
            msg.addFlashAttribute("error", "Ocurrió un error al editar el evento.");
            return "redirect:/event/" + eventDto.getId(); 
        }
    }


    // Método para eliminar evento
    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, Principal principal, RedirectAttributes msg,
                            @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            eventService.deleteEvent(id, principal.getName());
            msg.addFlashAttribute("success", "Evento eliminado con éxito.");
        } catch (RuntimeException e) {
            msg.addFlashAttribute("error", e.getMessage());
        }

        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

        boolean isAdmin = optionalUser
            .map(user -> "ROLE_ADMIN".equals(user.getRole()))
            .orElse(false);

        return isAdmin ? "redirect:/admin/allEvents" : "redirect:/user/profile";
    }


    // Método para denunciar evento
    @PostMapping("/{id}/report")
    public String reportEvent(@PathVariable Long id, @RequestParam("reason") ReportReason reason, Principal principal, 
                            RedirectAttributes msg) {
        if (principal == null) {
            msg.addFlashAttribute("error", "Debes iniciar sesión para reportar.");
            return "redirect:/event/" + id;
        }

        // Busca el evento
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        // Busca el usuario
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) {
            msg.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/event/" + id;
        }
        User user = optionalUser.get();

        // Chequea denuncia duplicada
        if (reportRepository.existsByEventAndUser(event, user)) {
            msg.addFlashAttribute("error", "Ya has denunciado este evento.");
            return "redirect:/event/" + id;
        }

        Report report = new Report();
        report.setEvent(event);
        report.setUser(user);
        report.setReason(reason);
        report.setCreatedAt(LocalDateTime.now());
        reportRepository.save(report);

        int totalReports = reportRepository.countByEventId(event.getId());

        // El evento denunciado se guarda en estado de revisión
        if (totalReports >= 3 && event.getStatus() != EventStatus.REVISION) {
            event.setStatus(EventStatus.REVISION);
        } else if (totalReports >= 1 && event.getStatus() == EventStatus.ACTIVO) {
            event.setStatus(EventStatus.DENUNCIADO);
        }

        eventRepository.save(event);
        
        // Notifica al admin
        msg.addFlashAttribute("success", "Gracias. Tu denuncia ha sido registrada.");

        return "redirect:/event/" + id;
    }


    // Método para cambiar el estado del evento (activo/inactivo)
	@PostMapping("/changeStatus")
	public String cambiarEstadoEvento(@RequestParam Long eventId,
									@RequestParam EventStatus nuevoEstado,
									@RequestParam(required = false) String redirectTo,
									RedirectAttributes msg) {
		Optional<Event> optionalEvent = eventRepository.findById(eventId);
		if (optionalEvent.isPresent()) {
			Event evento = optionalEvent.get();
			evento.setStatus(nuevoEstado);
			eventRepository.save(evento);
			msg.addFlashAttribute("success", "Estado del evento actualizado correctamente.");
		} else {
			msg.addFlashAttribute("error", "Evento no encontrado.");
		}
		return "redirect:/admin/allEvents";
	}

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
    public String publicarComentario(@PathVariable Long id, 
                                    @RequestParam String content, 
                                    @RequestParam(required = false) Long parentId,
                                    Principal principal) {

        // Busca el evento al que se le agregará el comentario
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        User user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado o no autorizado"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setEvent(event);
        comment.setUser(user);

        // Si es respuesta a otro comentario, busca el comentario padre
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario padre no encontrado"));
            comment.setParent(parentComment);
        }

        // Guardar el comentario
        commentRepository.save(comment);

        return "redirect:/event/" + id;
    }


    //Método para eliminar un comentario de un evento. - Solo el autor del comentario puede eliminarlo.
    @PostMapping("/{eventId}/comentario/{commentId}/delete")
    public String eliminarComentario(@PathVariable Long eventId, @PathVariable Long commentId, Principal principal) {

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        User user = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado o no autorizado"));

        // Solo el autor del comentario puede eliminarlo
        if (!Objects.equals(comment.getUser().getId(), user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar este comentario");
        }

        commentRepository.delete(comment);
        return "redirect:/event/" + eventId;
    }


}
