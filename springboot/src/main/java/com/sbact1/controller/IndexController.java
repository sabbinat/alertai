package com.sbact1.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.sbact1.model.Event;
import com.sbact1.model.EventStatus;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbact1.model.Category;

/**
 * Controlador principal para gestionar las rutas de la página de inicio, búsqueda, ayuda, contacto,
 * términos de uso, aviso de privacidad y visualización de perfiles de usuario.
 * 
 * Este controlador maneja la lógica para mostrar la página principal con eventos destacados,
 * búsqueda de usuarios, categorías y eventos, así como la visualización de información general
 * y perfiles de usuario.
 * 
 * Métodos principales:
 * - mostrarPaginaInicio: Muestra la página de inicio con eventos destacados y calendario.
 * - buscar: Permite realizar búsquedas por usuario, categoría o evento.
 * - help: Muestra la página de ayuda.
 * - contact: Muestra la página de contacto.
 * - terminosUso: Muestra la página de términos de uso.
 * - avisoPrivacidad: Muestra la página de aviso de privacidad.
 * - verPerfilUsuario: Permite visualizar el perfil de un usuario específico.
 * 
 */
@Controller
public class IndexController {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/")
    public String mostrarPaginaInicio(Model model, Principal principal, @AuthenticationPrincipal UserDetails userDetails) {
        List<EventStatus> visibleStatuses = List.of(EventStatus.ACTIVO, EventStatus.DENUNCIADO);
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        // Mapea los eventos por categoría ordenados por fecha
        Map<Long, List<Event>> eventsByCategory = new HashMap<>();
        for (Category cat : categories) {
        List<Event> evs = eventRepository.findByCategoryAndStatusInOrderByStartDateDesc(cat, visibleStatuses);
            eventsByCategory.put(cat.getId(), evs);
        }
        model.addAttribute("eventsByCategory", eventsByCategory);

        // Obtiene la categoría de entretenimiento para mostrar los eventos más esperados
        Optional<Category> entretenimiento = categoryRepository.findByName("Entretenimiento");

        List<Event> featuredEvents = new ArrayList<>();
        List<Event> eventos = new ArrayList<>();

        if (entretenimiento.isPresent()) {
            Category entretenimientoCategory = entretenimiento.get();

            // Eventos destacados
            featuredEvents = eventRepository
                .findByCategoryAndStatusInOrderByStartDateDesc(entretenimientoCategory, visibleStatuses)
                .stream()
                .sorted(Comparator.comparing(Event::getStartDate))
                .limit(3)
                .toList();

            // Eventos para el calendario
            eventos = eventRepository.findByCategoryAndStatusInOrderByStartDateDesc(entretenimientoCategory, visibleStatuses);
        }

        model.addAttribute("featuredEvents", featuredEvents);

        // Crear lista de eventos para el calendario
        List<Map<String, Object>> calendarEvents = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Event event : eventos) {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("title", event.getName());
            eventMap.put("start", event.getStartDate().format(formatter));
            eventMap.put("end", event.getEndDate() != null ? event.getEndDate().format(formatter) : null);
            eventMap.put("description", event.getDescription());
            eventMap.put("latitude", event.getLocation().getLatitude());
            eventMap.put("longitude", event.getLocation().getLongitude());

            calendarEvents.add(eventMap);
        }

        // La lista se convierte a formato JSON para integrarla en un calendario interactivo en la vista
        ObjectMapper mapper = new ObjectMapper();
        try {
            String eventsJson = mapper.writeValueAsString(calendarEvents);
            model.addAttribute("calendarEvents", eventsJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("calendarEvents", "[]");
        }

        return "index";
    }

    @GetMapping("/search")
    public String buscar(@RequestParam("query") String query, 
                        @RequestParam(value = "tipo", required = false, defaultValue = "todo") String tipo, Model model) {
        model.addAttribute("query", query);
        List<EventStatus> visibleStatuses = List.of(EventStatus.ACTIVO, EventStatus.DENUNCIADO);


        if (tipo.equals("usuario") || tipo.equals("todo")) {
            List<User> usuarios = userRepository.findByNameContainingIgnoreCase(query);
            if (!usuarios.isEmpty()) {
                User usuario = usuarios.get(0);
                model.addAttribute("usuario", usuario);
                model.addAttribute("eventosUsuario", eventRepository.findByUserAndStatusIn(usuario, visibleStatuses));
            }
        }

        if (tipo.equals("categoria") || tipo.equals("todo")) {
            List<Category> categorias = categoryRepository.findByNameContainingIgnoreCase(query);
            if (!categorias.isEmpty()) {
                Category categoria = categorias.get(0);
                model.addAttribute("categoria", categoria);
                model.addAttribute("eventosCategoria", eventRepository.findByCategoryAndStatusInOrderByStartDateDesc(categoria, visibleStatuses));
            }
        }

        if (tipo.equals("evento") || tipo.equals("todo")) {
            List<Event> eventos = eventRepository.findByNameContainingIgnoreCaseAndStatusIn(query, visibleStatuses);
            if (!eventos.isEmpty()) {
                Event evento = eventos.get(0);
                model.addAttribute("evento", evento);
            }
        }

        return "general/resultSearch";
    }

    @GetMapping("/help")
    public String help(Model model, Principal principal) {
        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }
        return "general/help"; 
    }

    @GetMapping("/contact")
    public String contact(Model model, Principal principal) {
        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }
        return "general/contact"; 
    }

    @GetMapping("/terminos")
    public String terminosUso(Principal principal, Model model) {
        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }
        return "general/terminos";
    }

    @GetMapping("/privacidad")
    public String avisoPrivacidad(Principal principal, Model model) {
        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }
        return "general/privacidad";
    }

    @GetMapping("/perfil/{id}")
    public String verPerfilUsuario(@PathVariable Integer id, Model model, Principal principal) {
        Optional<User> profileUserOptional = userRepository.findById(id);
        if (profileUserOptional.isEmpty()) {
            return "redirect:/event/allEvents?error=usuarioNoEncontrado";
        }

        User profileUser = profileUserOptional.get();
        model.addAttribute("profileUser", profileUser);

        // Cargar al usuario visitante (si está logueado)
        if (principal != null) {
            Optional<User> visitanteOptional = userRepository.findByEmail(principal.getName());
            visitanteOptional.ifPresent(visitante -> model.addAttribute("user", visitante));
        }

        // Resto del contenido accesible para todos (eventos, categorías)
        List<Event> events = eventRepository.findByUser(profileUser);
        model.addAttribute("events", events);

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        Map<Long, List<Event>> eventsByCategory = new HashMap<>();
        for (Category cat : categories) {
            List<Event> evs = eventRepository.findByUserAndCategoryOrderByStartDateDesc(profileUser, cat);
            eventsByCategory.put(cat.getId(), evs);
        }
        model.addAttribute("eventsByCategory", eventsByCategory);

        return "user/profile";
    }
}
