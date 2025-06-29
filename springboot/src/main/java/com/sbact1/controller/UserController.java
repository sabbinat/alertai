package com.sbact1.controller;


import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sbact1.dto.EventDto;
import com.sbact1.model.Category;
import com.sbact1.model.Comment;
import com.sbact1.model.Event;
import com.sbact1.model.EventStatus;
import com.sbact1.model.Report;
import com.sbact1.model.Token;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.CommentRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.ReportRepository;
import com.sbact1.repository.TokenRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.SettingService;
import com.sbact1.service.UserService;
import com.sbact1.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Controlador encargado de gestionar las operaciones relacionadas con el usuario en la aplicación.
 * Proporciona endpoints para la visualización y edición del perfil, gestión de eventos, preferencias,
 * notificaciones, configuración, eliminación de cuenta, contacto con el administrador y cambio de contraseña.
 * 
 * Funcionalidades principales:
 * 
 * - Mostrar la página principal del usuario con eventos, categorías y destacados.
 * - Visualizar y editar el perfil del usuario, incluyendo imagen y preferencias de categorías.
 * - Gestionar notificaciones de reportes y comentarios recibidos.
 * - Configurar opciones visuales y de idioma del sistema.
 * - Solicitar y confirmar la eliminación de la cuenta de usuario mediante token enviado por correo.
 * - Enviar consultas o dudas al administrador del sistema.
 * - Cambiar la contraseña del usuario autenticado.
 * 
 * Utiliza servicios y repositorios para interactuar con la base de datos y enviar correos electrónicos.
 * Incluye medidas de seguridad para verificar la autenticidad del usuario en cada operación sensible.
 * 
 * 
 * Cada método está documentado individualmente para detallar su propósito y parámetros.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private SettingService settingService;
    @Autowired private TokenRepository tokenRepository;
    @Autowired private EmailService emailService;
    @Autowired private UserService userService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private ReportRepository reportRepository;
    
    // Método que se ejecuta antes de cada petición para agregar el usuario logueado al modelo
    @ModelAttribute
    public void commonUser(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                m.addAttribute("user", optionalUser.get());
            }
        }
    }

    // Muestra la página principal del usuario, con eventos, categorías y destacados
    @GetMapping("/home")
    public String userHome(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userRepository.findByEmail(principal.getName());

        // Verificar si está presente
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/signin"; 
        }

        User user = userOptional.get();
        List<Event> myEvents = eventRepository.findByUser(user);
        if (myEvents == null) {
            myEvents = new ArrayList<>();
        }

        // Carga la configuración visual y de idioma
        settingService.cargarSettings(model, "/user/home");
        
        // Obtiene los eventos recientes
        List<EventStatus> visibleStatuses = List.of(EventStatus.ACTIVO, EventStatus.DENUNCIADO);

        // Eventos de otros usuarios visibles
        List<Event> recentEvents = eventRepository.findByUserNotAndStatusIn(user, visibleStatuses)
            .stream()
            .sorted(Comparator.comparing(Event::getRegistrationTime).reversed())
            .toList();


        Category alerta = categoryRepository.findByName("Alerta"); 

        // Filtra los eventos solo de esa categoría y toma los 4 más próximos
        List<Event> featuredEventsAlert = eventRepository
            .findByCategoryAndStatusInOrderByStartDateDesc(alerta, visibleStatuses)
            .stream()
            .sorted(Comparator.comparing(Event::getStartDate))
            .limit(4)
            .toList();

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("myEvents", eventRepository.findByUser(user));
        model.addAttribute("size", myEvents.size());
        model.addAttribute("idx", 0); 
        model.addAttribute("recentEvents", eventRepository.findAllByOrderByStartDateDesc()); 
        model.addAttribute("recentEvents", recentEvents);
        model.addAttribute("featuredEventsAlert", featuredEventsAlert);
        return "user/user_home";
    }

    // Muestra la página de perfil del usuario, con sus eventos organizados por categoría
    @GetMapping("/profile")
    public String perfilUsuario(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> user = userRepository.findByEmail(principal.getName());

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/signin";
        }

        User userAccount = user.get();
        settingService.cargarSettings(model, "/user/profile");

        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", new Locale("es", "ES"));

        Map<Long, List<EventDto>> eventsByCategory = new HashMap<>();

        for (Category cat : categories) {
            List<Event> evs = eventRepository.findByUserAndCategoryOrderByStartDateDesc(userAccount, cat);
            List<EventDto> eventDtos = evs.stream()
                .map(e -> new EventDto(e, formatter))
                .collect(Collectors.toList());
            eventsByCategory.put(cat.getId(), eventDtos);
        }

        Long userId = Long.valueOf(userAccount.getId());

        List<Report> reportesRecibidos = reportRepository.findByEventOwnerIdAndVistoFalse(userId);
        List<Comment> comentariosRecibidos = commentRepository.findByEventOwnerIdAndVistoFalse(userId);

        List<Event> eventosPreferidos = eventRepository.findByCategoryInAndUserNotOrderByRegistrationTimeDesc(
            userAccount.getNotificaciones(), userAccount)
            .stream()
            .limit(3) 
            .toList();

        int cantidadNotificaciones = reportesRecibidos.size() + comentariosRecibidos.size();

        model.addAttribute("user", userAccount);         
        model.addAttribute("profileUser", userAccount);

        model.addAttribute("eventosPreferidos", eventosPreferidos);
        model.addAttribute("cantidadNotificaciones", cantidadNotificaciones);
        model.addAttribute("eventsByCategory", eventsByCategory);
        model.addAttribute("comentariosRecibidos", comentariosRecibidos);
        model.addAttribute("reportesRecibidos", reportesRecibidos);

        return "user/profile";
    }

    // Meétodo que permite visualizar el perfil del usuario desde una cuenta externa
	@GetMapping("/{id}")
	public String verPerfilUsuario(@PathVariable Integer id, Model model, Principal principal) {
		Optional<User> profileUserOptional = userRepository.findById(id);
		Optional<User> visitanteOptional = userRepository.findByEmail(principal.getName());

		if (profileUserOptional.isEmpty() || visitanteOptional.isEmpty()) {
			return "redirect:/user/home?error=usuarioNoEncontrado";
		}

		User profileUser = profileUserOptional.get(); // dueño del perfil
		User visitante = visitanteOptional.get();     // el que está mirando 

		model.addAttribute("user", visitante);
		model.addAttribute("profileUser", profileUser);

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

    @PostMapping("/notificaciones/vistas")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> marcarVistas(Authentication auth) {
        User user = userService.getByEmail(auth.getName());
        Long userId = Long.valueOf(user.getId());

        reportRepository.marcarReportesComoVistos(userId);
        commentRepository.marcarComentariosComoVistos(userId);

        return ResponseEntity.ok().build();
    }


    // Actualiza los datos del perfil del usuario, incluyendo imagen
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser, 
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, 
                                @RequestParam(value = "phoneVerified", required = false) boolean phoneVerified,
                                Principal principal) {
        userService.updateUserProfile(updatedUser, imageFile, principal.getName(), phoneVerified);
        
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
        if (optionalUser.isEmpty()) {
            return "redirect:/signin"; 
        }
        User user = optionalUser.get();

        if ("ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/admin/home";
        } else {
            return "redirect:/user/profile";
        }

    }

    
    // Guarda las preferencias de categorías seleccionadas por el usuario
    @GetMapping("/preferences")
    public String showPreferences(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/signin";
        }
        User user = optionalUser.get();
        List<Category> categorias = categoryRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("categorias", categorias);

        return "user/profile"; 
    }

    @PostMapping("/preferences")
    public String savePreferences(@RequestParam List<Long> categoriaIds, 
                                Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/signin";
        }

        User user = optionalUser.get();
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

    @PostMapping("/settings")
    public String guardarSettings(@RequestParam String language,
            @RequestParam String redirectTo,
            RedirectAttributes redirectAttributes) {
        return settingService.guardarSettings(language, redirectTo, redirectAttributes);
    }


    // Método que usa el usuario para eliminar su cuenta (se le envía un correo para confirmar)
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes msg){
        try {
            userService.eliminarUsuario(id);
            msg.addFlashAttribute("sucessoEliminar", "Usuario eliminado exitosamente!");
        } catch (EntityNotFoundException e) {
            msg.addFlashAttribute("errorEliminar", "Usuario no encontrado");
            return "redirect:/signin";
        } catch (IllegalStateException e) {
            msg.addFlashAttribute("errorEliminar", e.getMessage());
            return "redirect:/user/profile"; // o donde tenga sentido
        }
        return "redirect:/signin";
    }

    @PostMapping("/solicitar-eliminacion")
    public String solicitarEliminacion(Principal principal, RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userRepository.findByEmail(principal.getName());

        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorEliminar", "Usuario no encontrado.");
            return "redirect:/signin";
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();

        Token deleteToken = new Token();
        deleteToken.setToken(token);
        deleteToken.setUser(user);
        deleteToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(deleteToken);

        String enlace = "http://localhost:8080/user/confirmar-eliminacion?token=" + token;

        try {
            emailService.enviarCorreo(user.getEmail(), "Confirmación para eliminar tu cuenta",
                "Haz clic en el siguiente enlace para confirmar la eliminación de tu cuenta: <br><a href='" + enlace + "'>Eliminar cuenta</a>");
            redirectAttributes.addFlashAttribute("successEliminar", "Se envió un enlace de confirmación a tu correo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorEliminar", "Error al enviar correo de confirmación.");
        }

        return "redirect:/user/profile";
    }

    @GetMapping("/confirmar-eliminacion")
    public String confirmarEliminacion(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        Optional<Token> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty() || optionalToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorEliminar", "Token inválido o expirado.");
            return "redirect:/signin";
        }

        User user = optionalToken.get().getUser();
        tokenRepository.delete(optionalToken.get());

        userService.eliminarUsuario(user.getId());
        redirectAttributes.addFlashAttribute("sucessoEliminar", "Tu cuenta ha sido eliminada exitosamente.");

        return "redirect:/signin";
    }


    // Método para contactarse con el admin o quien maneje el sistema
    @PostMapping("/enviarConsulta")
    public String enviarConsulta(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String asunto,
            @RequestParam String message,
            Model model) {
        try {
            emailService.enviarDudaUsuario(name, email, asunto, message);
            model.addAttribute("exito", "Tu mensaje fue enviado con éxito.");
        } catch (MessagingException | UnsupportedEncodingException e) {
            model.addAttribute("error", "Hubo un problema al enviar el mensaje.");
        }
        return "/general/contact"; 
    }

    // Método para cambiar la contraseña desde configuración
    @PostMapping("/change-password")
    public String cambiarPassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/user/profile";
        }

        boolean result = userService.cambiarPassword(principal.getName(), currentPassword, newPassword);
        if (!result) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente.");
        }

        return "redirect:/user/profile";
    }

}
