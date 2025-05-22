package com.sbact1.controller;

import com.sbact1.dto.EventDto;
import com.sbact1.model.Event;
import com.sbact1.model.Report;
import com.sbact1.model.ReportReason;
import com.sbact1.model.User;
import com.sbact1.repository.CommentRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.ReportRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.EmailService;
import com.sbact1.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import static org.springframework.http.HttpStatus.NOT_FOUND;


import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/event")
public class EventController {

    @Autowired private EventService eventService;
    @Autowired private EventRepository eventRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private EmailService emailService; 

    //Método que guarda los eventos creados
    @PostMapping("/save")
    public String saveEvent(@ModelAttribute EventDto eventDto, Principal principal,@RequestParam("categoryId") Long categoryId,
                            @RequestParam("image") MultipartFile imageFile) throws Exception {

        Event evento = eventService.saveEvent(eventDto, imageFile, principal.getName(), categoryId);

        List<User> interesados = userRepository.findByNotificacionesContaining(evento.getCategory());
        for (User user : interesados) {
            emailService.sendEmail(
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
    public String allEvents(Model model) {
        List<Event> allEvents = eventService.getAllEvents();

        Map<String, List<Event>> eventsByCategory = allEvents.stream()
            .collect(Collectors.groupingBy(e -> e.getCategory().getName()));

        model.addAttribute("eventsByCategory", eventsByCategory);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email);
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", null);
        }

        return "event/allEvent";
    }

    //Muestra los detalles de un evento específico
    @GetMapping("/{id}")
    public String showEvent(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("comments", commentRepository.findByEventId(id));

        // Formateo de fecha con día de la semana, nombre del mes, año
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        String formattedDate = event.getStartDate().format(formatter);
        model.addAttribute("formattedDate", formattedDate);

        //Carga el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String email = auth.getName();
            User user = userRepository.findByEmail(email);
            model.addAttribute("user", user);
        } else {
            model.addAttribute("user", null);
        }

        //Posibles motivos de denuncia
        model.addAttribute("reportReasons", ReportReason.values());

        return "event/event";
    }

    //Actualiza un evento existente
    @PostMapping("/updateEvent")
    public String updateEvent(@ModelAttribute EventDto eventDto, @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                          Principal principal) throws Exception {
        eventService.updateEvent(eventDto, imageFile, principal.getName());
        return "redirect:/user/profile";
    }

    //Elimina un evento existente
    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, Principal principal) {
        eventService.deleteEvent(id, principal.getName());
        return "redirect:/user/profile";
    }

    //Método para denunciar evento
     @PostMapping("/{id}/report")
    public String reportEvent(@PathVariable Long id, @RequestParam("reason") ReportReason reason, Principal principal,
            RedirectAttributes ra) {
        // Buscar evento 
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        // Usuario logueado
        if (principal == null) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para reportar.");
            return "redirect:/event/" + id;
        }
        User user = userRepository.findByEmail(principal.getName());

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
