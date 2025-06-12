package com.sbact1.controller;

import com.sbact1.dto.EventDto;
import com.sbact1.model.Category;
import com.sbact1.model.Event;
import com.sbact1.model.Report;
import com.sbact1.model.ReportReason;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.CommentRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.ReportRepository;
import com.sbact1.repository.UserRepository;
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
import java.util.Optional;

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

    //Método que guarda los eventos creados
    @PostMapping("/save")
    public String saveEvent(@ModelAttribute EventDto eventDto, Principal principal,@RequestParam("categoryId") Long categoryId) throws Exception {

        Event evento = eventService.saveEvent(eventDto, eventDto.getImageFile(), principal.getName(), categoryId);

        List<User> interesados = userRepository.findByNotificacionesContaining(evento.getCategory());
        for (User user : interesados) {
            emailService.enviarCorreo(
                user.getEmail(),
                "Nuevo evento en " + evento.getCategory().getName(),
                "Hola " + user.getName() + ",\n\n" +
                "Tenemos buenas noticias para ti. Se ha publicado un nuevo evento que coincide con tus intereses:\n\n" +
                "Nombre: " + evento.getName() + "\n" +
                "Descripción: " + evento.getDescription() + "\n" +
                "Fecha: " + evento.getStartDate().toString() + "\n" +
                "Categoría: " + evento.getCategory().getName() + "\n\n" +
                "¡No te lo pierdas! Puedes consultar más detalles en nuestro sitio web.\n\n" +
                "Gracias por ser parte de nuestra comunidad,\n" +
                "El equipo de AlertAi"            
            );
        }

        return "redirect:/user/home";
    }

    //Método que muestra todos los eventos agrupados por categoría
    @GetMapping("/allEvents")
    public String listarTodosLosEventos(@RequestParam(value = "name", required = false) String name, 
                                        @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                                        @RequestParam(value = "mes", required = false) Integer mes,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        Model model,
                                        Principal principal) {

        Pageable pageable = PageRequest.of(page, 12); 
        Page<Event> eventosPage;

        if (name != null && !name.isEmpty() && categoriaId != null && mes != null) {
            eventosPage = eventRepository.findByNameContainingIgnoreCaseAndCategoryIdAndMonth(name, categoriaId, mes, pageable);
        } else if (name != null && !name.isEmpty() && categoriaId != null) {
            eventosPage = eventRepository.findByNameContainingIgnoreCaseAndCategoryId(name, categoriaId, pageable);
        } else if (name != null && !name.isEmpty() && mes != null) {
            eventosPage = eventRepository.findByNameContainingIgnoreCaseAndMonth(name, mes, pageable);
        } else if (categoriaId != null && mes != null) {
            eventosPage = eventRepository.findByCategoryIdAndMonth(categoriaId, mes, pageable);
        } else if (categoriaId != null) {
            eventosPage = eventRepository.findByCategoryId(categoriaId, pageable);
        } else if (name != null && !name.isEmpty()) {
            eventosPage = eventRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (mes != null) {
            eventosPage = eventRepository.findByMonth(mes, pageable);
        } else {
            eventosPage = eventRepository.findAll(pageable);
        }


        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("events", eventosPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("name", name);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("mes", mes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventosPage.getTotalPages());

        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }

        return "event/allEvents";
    }

    //Muestra los detalles de un evento específico
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

    //Actualiza un evento existente
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
                            Principal principal) throws Exception {

        eventService.updateEvent(eventDto, imageFile, principal.getName());

        if (redirectTo != null && !redirectTo.isBlank()) {
            return "redirect:" + redirectTo;
        }

        return "redirect:/user/profile";
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Evento eliminado con éxito.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

        boolean isAdmin = optionalUser
            .map(user -> "ROLE_ADMIN".equals(user.getRole()))
            .orElse(false);

        return isAdmin ? "redirect:/admin/allEvents" : "redirect:/user/profile";
    }

    //Método para denunciar evento
    @PostMapping("/{id}/report")
    public String reportEvent(@PathVariable Long id, @RequestParam("reason") ReportReason reason, Principal principal, RedirectAttributes ra) {
        // Verificar que el usuario esté logueado
        if (principal == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para reportar.");
            return "redirect:/event/" + id;
        }

        // Buscar el evento
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        // Buscar el usuario
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) {
            ra.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/event/" + id;
        }
        User user = optionalUser.get();

        // Chequear duplicado
        if (reportRepository.existsByEventAndUser(event, user)) {
            ra.addFlashAttribute("error", "Ya has denunciado este evento.");
            return "redirect:/event/" + id;
        }

        // Guardar denuncia
        Report report = new Report();
        report.setEvent(event);
        report.setUser(user);
        report.setReason(reason);
        report.setCreatedAt(LocalDateTime.now());
        reportRepository.save(report);

        // Notificar al admin
        ra.addFlashAttribute("success", "Gracias. Tu denuncia ha sido registrada.");

        return "redirect:/event/" + id;
    }

}
