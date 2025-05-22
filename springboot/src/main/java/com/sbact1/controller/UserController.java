package com.sbact1.controller;


import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sbact1.model.Category;
import com.sbact1.model.Event;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.SettingService;
import com.sbact1.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserService userService;
    @Autowired    
    private SettingService settingService;
    
    // Método que se ejecuta antes de cada petición para agregar el usuario logueado al modelo
    @ModelAttribute
    public void commonUser(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            User user = userRepository.findByEmail(email);
            m.addAttribute("user", user);
        }
    }

    // Muestra la página principal del usuario, con eventos, categorías y destacados
    @GetMapping("/home")
    public String userHome(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<Event> myEvents = eventRepository.findByUser(user);
        if (myEvents == null) {
            myEvents = new ArrayList<>();
        }

        // Carga la configuración visual y de idioma
        settingService.cargarSettings(model, "/user/home");

        // Añade categorías, eventos del usuario y eventos recientes
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("myEvents", eventRepository.findByUser(user));
        model.addAttribute("size", myEvents.size());
        model.addAttribute("idx", 0); 
        model.addAttribute("recentEvents", eventRepository.findAllByOrderByStartDateDesc());  
        
        // Obtiene los eventos recientes
        List<Event> recentEvents = eventRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(Event::getRegistrationTime).reversed())
            .toList();

        model.addAttribute("recentEvents", recentEvents);
        

        // Obtener la categoría de entretenimiento (por nombre o ID)
        Category alerta = categoryRepository.findByName("Alerta"); 

        // Filtrar los eventos solo de esa categoría y tomar los 3 más próximos
        List<Event> featuredEventsAlert = eventRepository.findByCategoryOrderByStartDateDesc(alerta)
            .stream()
            .sorted(Comparator.comparing(Event::getStartDate))
            .limit(4)
            .toList();

        model.addAttribute("featuredEventsAlert", featuredEventsAlert);
        return "user/user_home";
    }

    // Muestra la página de perfil del usuario, con sus eventos organizados por categoría
    @GetMapping("/profile")
    public String perfilUsuario(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        settingService.cargarSettings(model, "/user/profile");

        model.addAttribute("events", eventRepository.findByUser(user));

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        // Mapear los eventos por categoría ordenados por fecha
        Map<Long, List<Event>> eventsByCategory = new HashMap<>();
        for (Category cat : categories) {
            List<Event> evs = eventRepository.findByUserAndCategoryOrderByStartDateDesc(user, cat);
            eventsByCategory.put(cat.getId(), evs);
        }
        model.addAttribute("eventsByCategory", eventsByCategory);
        return "user/profile";
    }

    // Actualiza los datos del perfil del usuario, incluyendo imagen
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User user,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            Principal principal) {
    userService.updateUserProfile(user, imageFile, principal.getName());

    User currentUser = userRepository.findByEmail(principal.getName());
    if (currentUser.getRole().equals("ROLE_ADMIN")) {
        return "redirect:/admin/home";
    } else {
        return "redirect:/user/profile";
    }
}

    // Muestra el formulario para configurar las preferencias de categorías
    @GetMapping("/preferences")
    public String showPreferences(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<Category> categorias = categoryRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("categorias", categorias);

        return "user/profile"; 
    }

    // Guarda las preferencias de categorías seleccionadas por el usuario
    @PostMapping("/preferences")
    public String savePreferences(@RequestParam List<Long> categoriaIds, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<Category> categoriasSeleccionadas = categoryRepository.findAllById(categoriaIds);
        user.setNotificaciones(categoriasSeleccionadas);
        userRepository.save(user);
        return "redirect:/user/profile"; 
    }
    
    // Muestra la página de configuración del sistema (idioma, visual, etc.)
    @GetMapping("/settings")
    public String mostrarSettings(Model model, Principal principal,
            @RequestParam(name = "redirectTo", required = false, defaultValue = "/user/home") String redirectTo) {
        settingService.cargarSettings(model, redirectTo);
        return "settings";
    }

    // Guarda las configuraciones del sistema (idioma, etc.)
    @PostMapping("/settings")
    public String guardarSettings(@RequestParam String language,
            @RequestParam String redirectTo,
            RedirectAttributes redirectAttributes) {
        return settingService.guardarSettings(language, redirectTo, redirectAttributes);
    }

    // Elimina un usuario, solo si no tiene eventos o comentarios asociados
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes msg){
        Optional<User> categoria = userRepository.findById(id); 
        if(categoria.isEmpty()) {
            msg.addFlashAttribute("errorEliminar", "No puedes eliminar este usuario porque tiene eventos o comentarios registrados.");
            return "redirect:/admin/users/";
        }
        userService.eliminarUsuario(id); 
        msg.addFlashAttribute("sucessoEliminar", "Usuario eliminado exitosamente!");
        return "redirect:/admin/users/";
    }
}
