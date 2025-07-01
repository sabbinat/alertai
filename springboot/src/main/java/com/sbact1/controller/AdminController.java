package com.sbact1.controller;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbact1.model.*;
import com.sbact1.repository.*;
import com.sbact1.service.EmailService;
import com.sbact1.service.UserService;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.format.DateTimeFormatter;

/**
 * Controlador para la administración del sistema.
 * Proporciona funcionalidades para la gestión de usuarios, eventos, denuncias y categorías.
 * 
 * Funcionalidades principales:
 * 
 *   Dashboard de administración con estadísticas y últimos registros.
 *   Gestión de usuarios: listado, filtrado, cambio de rol, eliminación y restablecimiento de contraseña.
 *   Gestión de denuncias: listado, filtrado, aprobación, rechazo y eliminación de denuncias.
 *   Gestión de eventos: listado y filtrado de eventos por categoría y estado.
 *   Gestión de categorías: listado de categorías y conteo de eventos por categoría.
 * 
 * Seguridad: Solo accesible para usuarios con rol de administrador.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired private UserRepository userRepository;
	@Autowired private ReportRepository reportRepository;
	@Autowired private CategoryRepository categoryRepository;
	@Autowired private UserService userService;
	@Autowired private EventRepository eventRepository;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private EmailService emailService;

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
		
		// Toma los 4 últimos usuarios registrados y formatear sus datos para mostrar
		List<Map<String, String>> latestUsersFormatted = users.stream()
		    .filter(u -> !"ROLE_ADMIN".equals(u.getRole()))
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
		List<Report> reports = reportRepository.findAll().stream()
			.filter(r -> !r.isReviewed()) 
			.sorted(Comparator.comparing(Report::getCreatedAt).reversed())
			.limit(5)
			.collect(Collectors.toList());
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

		long eventosActivos = eventos.stream().filter(e -> e.getStatus() == EventStatus.ACTIVO).count();
		long eventosInactivos = eventos.stream().filter(e -> e.getStatus() == EventStatus.INACTIVO).count();
		long eventosDenunciados = eventos.stream().filter(e -> e.getStatus() == EventStatus.DENUNCIADO).count();
		long eventosRevision = eventos.stream().filter(e -> e.getStatus() == EventStatus.REVISION).count();

		model.addAttribute("eventosActivos", eventosActivos);
		model.addAttribute("eventosInactivos", eventosInactivos);
		model.addAttribute("eventosDenunciados", eventosDenunciados);
		model.addAttribute("eventosRevision", eventosRevision);


		return "admin/admin_home";
	}


	// Método que gestiona los usuarios ingresados en el sistema
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

		// Mapear los resultados a un Map<userId, count>
		Map<Integer, Long> eventosPorUsuario = eventRepository.countEventsByUser().stream()
			.collect(Collectors.toMap(
				row -> (Integer) row[0],
				row -> (Long) row[1]
			));

		model.addAttribute("eventosPorUsuario", eventosPorUsuario);
		model.addAttribute("users", usersPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", usersPage.getTotalPages());
		model.addAttribute("rolFiltro", rolFiltro);
		model.addAttribute("roles", List.of("ROLE_USER", "ROLE_ADMIN"));

		return "admin/admin_listUser";
	}

	// Método que elimina usuario
	@GetMapping("/eliminar/{id}")
	public String deleteUser(@PathVariable Integer id, RedirectAttributes msg){ 
	    Optional<User> categoria = userRepository.findById(id); 
	    if(categoria.isEmpty()) {
	        msg.addFlashAttribute("errorEliminar", "Ususario no encontrado");
	        return "redirect:/admin/users";
	    }
        userService.deleteUser(id); 
	    msg.addFlashAttribute("sucessoEliminar", "Usuario eliminado exitosamente!");
	    return "redirect:/admin/users";
	}

	// Método para cambiar el rol de los usuarios
	@PostMapping("/cambiar-rol")
	public String changeRole(@RequestParam Integer userId, @RequestParam String newRole) {
		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setRole(newRole); 
			userRepository.save(user);
		}
		return "redirect:/admin/users"; 
	}

	// Método para que el admin cambie la contraseña del usuario
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam int userId, RedirectAttributes redirectAttributes) {
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
			return "redirect:/admin/users";
		}

		User user = optionalUser.get();

		// Generar contraseña temporal (puedes cambiar el método por uno más seguro si quieres)
		String tempPassword = "Temporal123@"; 
		
		// Cambiar la contraseña del usuario
		user.setPassword(passwordEncoder.encode(tempPassword));
		userRepository.save(user);

		// Opcional: enviar un correo notificando al usuario sobre la nueva contraseña temporal
		String subject = "Contraseña restablecida";
		String content = "<p>Hola " + user.getName() + ",</p>"
					+ "<p>Tu contraseña ha sido restablecida por un administrador. "
					+ "Tu nueva contraseña temporal es: <strong>" + tempPassword + "</strong></p>"
					+ "<p>Por favor, ingresa y cambia tu contraseña.</p>";
		try {
			emailService.enviarCorreo(user.getEmail(), subject, content);
		} catch (Exception e) {
			// Si falla el email, no bloqueamos la acción pero podemos avisar al admin
			redirectAttributes.addFlashAttribute("warning", "Contraseña restablecida, pero no se pudo enviar el correo.");
			return "redirect:/admin/users";
		}

		redirectAttributes.addFlashAttribute("success", "Contraseña restablecida y correo enviado al usuario.");
		return "redirect:/admin/users";
	}

	// Método que gestiona los eventos
	@GetMapping("/allEvents")
	public String listEvents(
		@RequestParam(required = false) Long categoriaId,
		@RequestParam(required = false) List<EventStatus> estadosFiltro,
		@RequestParam(required = false) String name,
		@RequestParam(value = "page", defaultValue = "0") int page,
		Model model) {

		Pageable pageable = PageRequest.of(page, 10);
		Page<Event> eventosPage;

		if (categoriaId != null && estadosFiltro != null && !estadosFiltro.isEmpty()) {
			eventosPage = eventRepository.findByCategoryIdAndStatusIn(categoriaId, estadosFiltro, pageable);
		} else if (categoriaId != null) {
			eventosPage = eventRepository.findByCategoryId(categoriaId, pageable);
		} else if (estadosFiltro != null && !estadosFiltro.isEmpty()) {
			eventosPage = eventRepository.findByStatusIn(estadosFiltro, pageable);
		} else {
			eventosPage = eventRepository.findAll(pageable);
		}

		model.addAttribute("eventos", eventosPage.getContent());
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("categoriaId", categoriaId);
		model.addAttribute("estados", List.of(EventStatus.ACTIVO, EventStatus.INACTIVO));
		model.addAttribute("estadosFiltro", estadosFiltro);
		model.addAttribute("EventStatus", EventStatus.class); 
		model.addAttribute("name", name);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", eventosPage.getTotalPages());

		return "admin/admin_allEvents";
	}


	// Métodos que gestionan las denuncias
	@GetMapping("/reports")
	public String listReports(
		@RequestParam(required = false) ReportReason motivoFiltro,
		@RequestParam(required = false) EventStatus estadoFiltro,
		Model model) {

		List<Report> reports;

		if (motivoFiltro != null && estadoFiltro != null) {
			reports = reportRepository.findByReviewedFalseAndReasonAndEventStatus(motivoFiltro, estadoFiltro);
		} else if (motivoFiltro != null) {
			reports = reportRepository.findByReviewedFalseAndReason(motivoFiltro);
		} else if (estadoFiltro != null) {
			reports = reportRepository.findByReviewedFalseAndEventStatus(estadoFiltro);
		} else {
			reports = reportRepository.findByReviewedFalse();
		}

		Map<Long, Long> reportCounts = reports.stream()
        .collect(Collectors.groupingBy(r -> r.getEvent().getId(), Collectors.counting()));

		model.addAttribute("reports", reports);
		model.addAttribute("reportCounts", reportCounts); 
		model.addAttribute("motivos", ReportReason.values());
		model.addAttribute("estadoFiltro", estadoFiltro);
		model.addAttribute("estados", List.of(EventStatus.REVISION, EventStatus.INACTIVO, EventStatus.DENUNCIADO));
		model.addAttribute("motivoFiltro", motivoFiltro);

		return "admin/admin_reports";
	}

	// Denuncia revisada y aporbada 
	@PostMapping("/event/{id}/approve")
	public String approveEvent(@PathVariable Long id, RedirectAttributes ra) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

		event.setStatus(EventStatus.ACTIVO);
		eventRepository.save(event);

		// Marca todas las denuncias del evento como revisadas
		List<Report> reports = reportRepository.findByEventId(id);
		for (Report report : reports) {
			report.setReviewed(true);
		}
		reportRepository.saveAll(reports);
		
		ra.addFlashAttribute("success", "El evento fue aprobado exitosamente y está disponible para los usuarios.");
		return "redirect:/admin/reports"; 
	}

	// Denuncia rechazada
	@PostMapping("/event/{id}/reject")
	public String rejectEvent(@PathVariable Long id, RedirectAttributes ra) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

		event.setStatus(EventStatus.INACTIVO);
		eventRepository.save(event);
		ra.addFlashAttribute("success", "La revisión fue completada: el evento ha sido rechazado y desactivado.");
		return "redirect:/admin/reports";
	}

	// Denuncia eliminada
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


	// Método que gestiona las categorías
	@GetMapping("/allCategories")
	public String listCategories(Model model, Principal principal) {
		List<Category> categories = categoryRepository.findAll();
		// Obtener la cantidad de eventos para cada categoría
		Map<Long, Long> eventCountByCategory = new HashMap<>();

		for (Category category : categories) {
			long count = eventRepository.countByCategoryId(category.getId());
			eventCountByCategory.put(category.getId(), count);
		}

		model.addAttribute("categories", categories);
		model.addAttribute("eventCountByCategory", eventCountByCategory);
		return "admin/admin_allCategories";
	}


}
