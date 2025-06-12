package com.sbact1.service;

import com.sbact1.dto.EventDto;
import com.sbact1.model.Category;
import com.sbact1.model.Event;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.model.Location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    //Guarda un nuevo evento utilizando los datos del DTO, una imagen, el email del usuario y el ID de la categoría.
    public Event saveEvent(EventDto dto, MultipartFile imageFile, String email, Long categoryId) throws Exception {
        Event event = new Event();
        event.setName(dto.getName());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setTime(dto.getTime());
        event.setDescription(dto.getDescription());
        event.setContact(dto.getContact());

        // Si se proporciona una imagen, se guarda
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/events");
            Files.createDirectories(uploadPath);
            try (InputStream in = imageFile.getInputStream()) {
                Files.copy(in, uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
            event.setImage(filename);
        }

        // Buscar categoría
        Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        event.setCategory(cat);

        // Buscar usuario
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        event.setUser(user);

        // Ubicación
        Location location = new Location();
        location.setAddress(dto.getLocation().getAddress());
        location.setLatitude(dto.getLocation().getLatitude());
        location.setLongitude(dto.getLocation().getLongitude());
        event.setLocation(location);

        return eventRepository.save(event); 
    }

    //Actualiza un evento existente. Solo el creador del evento puede editarlo.
    public void updateEvent(EventDto dto, MultipartFile file, String username) throws Exception {
        //Busca el evento por ID
        Event event = eventRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Verifica que el usuario autenticado sea el propietario del evento
        if (!event.getUser().getEmail().equals(username)) {
            throw new RuntimeException("No autorizado para editar este evento");
        }

        // Actualiza los datos del evento
        event.setName(dto.getName());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setTime(dto.getTime());
        event.setContact(dto.getContact());

        //Actualiza la ubicación
        Location loc = event.getLocation();
        if (loc == null) {
            loc = new Location();
        }
        
        loc.setAddress(dto.getLocation().getAddress());
        loc.setLatitude(dto.getLocation().getLatitude());
        loc.setLongitude(dto.getLocation().getLongitude());
        event.setLocation(loc);

        // Si se proporciona una nueva imagen, se guarda
        if (!file.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads/events");
            Files.createDirectories(uploadPath);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
            event.setImage(filename);
        }

        eventRepository.save(event);
    }

    //Elimina un evento. 
    public void deleteEvent(Long id, String username) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isOwner = event.getUser() != null && event.getUser().getEmail().equals(username);
        boolean isAdmin = user.getRole() != null && user.getRole().contains("ROLE_ADMIN");

        if (isOwner || isAdmin) {
            eventRepository.deleteById(id);
        } else {
            throw new RuntimeException("No estás autorizado para eliminar este evento.");
        }
    }

    //Busca un evento por su ID.
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    //Lista los eventos con paginación y filtro opcional por categoría.
    public Page<Event> listarEventos(Long categoriaId, Pageable pageable) {
        if (categoriaId == null) {
            return eventRepository.findAll(pageable);
        }
        return eventRepository.findByCategoryId(categoriaId, pageable);
    }

    //Lista todos los eventos
    public List<Event> findAll() {
        return eventRepository.findAll();
    }
}
