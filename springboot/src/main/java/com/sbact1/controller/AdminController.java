package com.sbact1.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbact1.model.Category;
import com.sbact1.model.Event;
import com.sbact1.model.Report;
import com.sbact1.model.ReportReason;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.ReportRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired private UserRepository userRepository;
	@Autowired private ReportRepository reportRepository;
	@Autowired private CategoryRepository categoryRepository;
	@Autowired private UserService userService;
	@Autowired private EventRepository eventRepository;

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

	// Método para la página principal del admin (/admin/home)
	@GetMapping("/home")
	public String userHome(@RequestParam(value = "metaUsuarios", required = false, defaultValue = "100") int metaUsuarios, Model model) {
		List<User> users = userRepository.findAll();
        model.addAttribute("users", users); 

		// Formato para mostrar fechas en español (ej. lunes, 22 mayo 2025)
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("es"));
		
		// Toma los 5 últimos usuarios registrados y formatear sus datos para mostrar
		List<Map<String, String>> latestUsersFormatted = users.stream()
			.sorted(Comparator.comparing(User::getRegistrationTime).reversed())
			.limit(4)
			.map(u -> {
				Map<String, String> userMap = new HashMap<>();
				userMap.put("id", String.valueOf(u.getId()));
				userMap.put("name", u.getName());
				userMap.put("username", u.getUsername());
				userMap.put("image", u.getImage());
				userMap.put("registration", u.getRegistrationTime()
					.format(formatter)
					.substring(0, 1).toUpperCase() + 
					u.getRegistrationTime().format(formatter).substring(1));
				return userMap;
			})
			.collect(Collectors.toList());

		model.addAttribute("latestUsersFormatted", latestUsersFormatted);

		// Total de usuarios registrados
		long totalUsers = users.size();
        model.addAttribute("totalUsers", totalUsers); 

		// Calcula el porcentaje de usuarios en relación con una meta dada
		double porcentajeUsuarios = ((double) totalUsers / metaUsuarios) * 100;
		model.addAttribute("metaUsuarios", metaUsuarios);
		model.addAttribute("porcentajeUsuarios", porcentajeUsuarios);

		// Obtiene todas las denuncias y categorías para mostrar en el dashboard
		List<Report> reports = reportRepository.findAll();
        model.addAttribute("reports", reports);

		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories); 

		// Total de categorias
		long totalCategory = categories.size();
		model.addAttribute("totalCategory", totalCategory);

		// Prepara eventos para mostrar en un calendario (como JSON)
        List<Event> eventos = eventRepository.findAll();
		List<Map<String, Object>> calendarEvents = new ArrayList<>();

        DateTimeFormatter formatterE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		// Construye lista de mapas con info de eventos necesaria para el calendario
        for (Event event : eventos) {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("title", event.getName());
            eventMap.put("start", event.getStartDate().format(formatterE));
            eventMap.put("end", event.getEndDate() != null ? event.getEndDate().format(formatterE) : null);
            eventMap.put("description", event.getDescription());
            eventMap.put("latitude", event.getLocation().getLatitude());
            eventMap.put("longitude", event.getLocation().getLongitude());
            calendarEvents.add(eventMap);
        }

        // Convierte lista de eventos a JSON para que el frontend pueda procesarla
        ObjectMapper mapper = new ObjectMapper();
        try {
            String eventsJson = mapper.writeValueAsString(calendarEvents);
            model.addAttribute("calendarEvents", eventsJson);
        } catch (JsonProcessingException e) {
			// En caso de error JSON, envia lista vacía para no romper la vista
            model.addAttribute("calendarEvents", "[]");
        }

		return "admin/admin_home";
	}

	// Muestra lista de usuarios
	@GetMapping("/users")
	public String listUsers(@RequestParam(required = false) String rolFiltro, @RequestParam(defaultValue = "0") int page, Model model) {

		int pageSize = 10; 
		Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());

		Page<User> usersPage;

		if (rolFiltro != null && !rolFiltro.isEmpty()) {
			usersPage = userRepository.findByRole(rolFiltro, pageable);
		} else {
			usersPage = userRepository.findAll(pageable);
		}

		model.addAttribute("users", usersPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("rolFiltro", rolFiltro);
		model.addAttribute("roles", List.of("ROLE_USER", "ROLE_ADMIN"));

		return "admin/admin_listUser";
	}

	//Método para eliminar usuario
	@GetMapping("/eliminar/{id}")
	public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes msg){
	    Optional<User> categoria = userRepository.findById(id); 
	    if(categoria.isEmpty()) {
	        msg.addFlashAttribute("errorEliminar", "Ususario no encontrado");
	        return "redirect:/admin/users";
	    }
        userService.eliminarUsuario(id); 
	    msg.addFlashAttribute("sucessoEliminar", "Usuario eliminado exitosamente!");
	    return "redirect:/admin/users";
	}

	@PostMapping("/user/cambiar-rol")
	public String cambiarRol(@RequestParam Integer userId, @RequestParam String newRole) {
		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setRole(newRole); 
			userRepository.save(user);
		}
		return "redirect:/admin/admin_listUser"; 
	}

	@GetMapping("/reports")
	public String listReports(@RequestParam(required = false) ReportReason motivoFiltro, Model model) {
		List<Report> reports;

		if (motivoFiltro != null) {
			reports = reportRepository.findByReviewedFalseAndReason(motivoFiltro);
		} else {
			reports = reportRepository.findByReviewedFalse();
		}

		model.addAttribute("reports", reports);
		model.addAttribute("motivos", ReportReason.values()); 
		model.addAttribute("motivoFiltro", motivoFiltro);

		return "admin/admin_reports";
	}

	@GetMapping("/reports/handle/{id}")
	public String handleReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Report report = reportRepository.findById(id).orElse(null);

		if (report != null) {
			report.setReviewed(true);
			reportRepository.save(report);
			redirectAttributes.addFlashAttribute("success", "Denuncia marcada como revisada.");
		} else {
			redirectAttributes.addFlashAttribute("error", "La denuncia no existe.");
		}

		return "redirect:/admin/reports";
	}

	@GetMapping("/reports/delete/{id}")
	public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		Optional<Report> reportOptional = reportRepository.findById(id);

		if (reportOptional.isPresent()) {
			reportRepository.delete(reportOptional.get());
			redirectAttributes.addFlashAttribute("success", "Denuncia eliminada correctamente.");
		} else {
			redirectAttributes.addFlashAttribute("error", "La denuncia no existe.");
		}

		return "redirect:/admin/reports";
	}

	@GetMapping("/allEvents")
	public String listarTodosLosEventos(
		@RequestParam(value = "name", required = false) String name,
		@RequestParam(value = "categoriaId", required = false) Long categoriaId,
		@RequestParam(value = "page", defaultValue = "0") int page,
		Model model) {

		Pageable pageable = PageRequest.of(page, 10); 
		Page<Event> eventosPage;

		if ((name != null && !name.isEmpty()) && categoriaId != null) {
			eventosPage = eventRepository.findByNameContainingIgnoreCaseAndCategoryId(name, categoriaId, pageable);
		} else if (name != null && !name.isEmpty()) {
			eventosPage = eventRepository.findByNameContainingIgnoreCase(name, pageable);
		} else if (categoriaId != null) {
			eventosPage = eventRepository.findByCategoryId(categoriaId, pageable);
		} else {
			eventosPage = eventRepository.findAll(pageable);
		}

		List<Category> categories = categoryRepository.findAll();

		model.addAttribute("eventos", eventosPage.getContent());
		model.addAttribute("categories", categories);
		model.addAttribute("name", name);
		model.addAttribute("categoriaId", categoriaId);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", eventosPage.getTotalPages());

		return "admin/admin_allEvents";
	}

	@GetMapping("/allCategories")
	public String listCategories(Model model, Principal principal) {
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);

		return "admin/admin_allCategories";
	}

	@GetMapping("/user/{id}")
	public String verPerfilUsuarioDesdeAdmin(@PathVariable Integer id, Model model) {
		Optional<User> userOptional = userRepository.findById(id);
		if (userOptional.isEmpty()) {
			return "redirect:/admin/home?error=usuarioNoEncontrado";
		}

		User user = userOptional.get();
		model.addAttribute("user", user);

		model.addAttribute("events", eventRepository.findByUser(user));

		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("categories", categories);

		Map<Long, List<Event>> eventsByCategory = new HashMap<>();
		for (Category cat : categories) {
			List<Event> evs = eventRepository.findByUserAndCategoryOrderByStartDateDesc(user, cat);
			eventsByCategory.put(cat.getId(), evs);
		}
		model.addAttribute("eventsByCategory", eventsByCategory);

		return "user/profile";
	}


}
