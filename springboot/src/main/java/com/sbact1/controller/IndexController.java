package com.sbact1.controller;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sbact1.model.Event;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbact1.model.Category;


@Controller
public class IndexController {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    //Método que gestiona la lógica para mostrar la página de inicio 
    @GetMapping("/")
    public String mostrarPaginaInicio(Model model, Principal principal, @AuthenticationPrincipal UserDetails userDetails) {
        // Obtiene todas las categorías
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        // Mapea los eventos por categoría ordenados por fecha
        Map<Long, List<Event>> eventsByCategory = new HashMap<>();
        for (Category cat : categories) {
            List<Event> evs = eventRepository.findByCategoryOrderByStartDateDesc(cat);
            eventsByCategory.put(cat.getId(), evs);
        }
        model.addAttribute("eventsByCategory", eventsByCategory);

        // Obtiene la categoría de entretenimiento para mostrar los eventos más esperados
        Category entretenimiento = categoryRepository.findByName("Entretenimiento"); 

        // Filtra los eventos solo de esa categoría y toma los 3 más próximos
        List<Event> featuredEvents = eventRepository.findByCategoryOrderByStartDateDesc(entretenimiento)
            .stream()
            .sorted(Comparator.comparing(Event::getStartDate))
            .limit(3)
            .toList();

        model.addAttribute("featuredEvents", featuredEvents);

        //Se genera una lista de eventos (solo de entretenimiento) en formato de calendario
        List<Event> eventos = eventRepository.findByCategoryOrderByStartDateDesc(entretenimiento);

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

        return "/general/index";
    }

    //Método que permite realizar búsquedas en el sitio, ya sea por usuario, categoría o evento
    @GetMapping("/search")
    public String buscar(@RequestParam("query") String query, @RequestParam(value = "tipo", required = false, defaultValue = "todo") String tipo,
                        Model model) {

        model.addAttribute("query", query);

        if (tipo.equals("usuario") || tipo.equals("todo")) {
            List<User> usuarios = userRepository.findByNameContainingIgnoreCase(query);
            if (!usuarios.isEmpty()) {
                User usuario = usuarios.get(0);
                model.addAttribute("usuario", usuario);
                model.addAttribute("eventosUsuario", eventRepository.findByUser(usuario));
            }
        }

        if (tipo.equals("categoria") || tipo.equals("todo")) {
            List<Category> categorias = categoryRepository.findByNameContainingIgnoreCase(query);
            if (!categorias.isEmpty()) {
                Category categoria = categorias.get(0);
                model.addAttribute("categoria", categoria);
                model.addAttribute("eventosCategoria", eventRepository.findByCategoryOrderByStartDateDesc(categoria));
            }
        }

        if (tipo.equals("evento") || tipo.equals("todo")) {
            List<Event> eventos = eventRepository.findByNameContainingIgnoreCase(query);
            if (!eventos.isEmpty()) {
                Event evento = eventos.get(0);
                model.addAttribute("evento", evento);
            }
        }

        return "/search/resultSearch";
    }

}
