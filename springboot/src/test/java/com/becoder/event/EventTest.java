package com.becoder.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.sbact1.dto.EventDto;
import com.sbact1.model.Event;
import com.sbact1.model.Location;
import com.sbact1.model.User;
import com.sbact1.model.Category;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.EventService;


@ExtendWith(MockitoExtension.class)
class EventTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void guardarEvento() throws Exception {
        // Preparar datos del DTO
        EventDto dto = new EventDto();
        dto.setName("Festival de Música");
        dto.setStartDate(LocalDate.of(2025, 6, 20));
        dto.setEndDate(LocalDate.of(2025, 6, 21));
        dto.setTime(java.time.LocalTime.of(18, 0));
        dto.setDescription("Un gran festival con bandas en vivo");
        dto.setContact("festival@example.com");

        // Mock de imagen vacía (no se prueba la carga real del archivo)
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.isEmpty()).thenReturn(true);
        dto.setImageFile(mockFile);

        // Ubicación
        Location location = new Location();
        location.setAddress("Plaza Flores, Rivera");
        location.setLatitude(-30.904783);
        location.setLongitude(-55.5440212);
        dto.setLocation(location);

        // Datos simulados
        Category categoria = new Category();
        categoria.setId(1L);
        categoria.setName("Entretenimiento");

        User usuario = new User();
        usuario.setId(1);
        usuario.setEmail("usuario@ejemplo.com");
        usuario.setName("Carlos");

        // Simular comportamiento de los repos
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoria));
        Mockito.when(userRepository.findByEmail("usuario@ejemplo.com")).thenReturn(Optional.of(usuario));
        Mockito.when(eventRepository.save(Mockito.any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ejecutar método
        Event guardado = eventService.saveEvent(dto, mockFile, "usuario@ejemplo.com", 1L);

		System.out.println("Nombre: " + guardado.getName());
System.out.println("Categoría: " + guardado.getCategory().getName());
System.out.println("Usuario: " + guardado.getUser().getName());
System.out.println("Dirección: " + guardado.getLocation().getAddress());

        // Verificar resultado
        assertEquals("Festival de Música", guardado.getName());
        assertEquals("Entretenimiento", guardado.getCategory().getName());
        assertEquals("Carlos", guardado.getUser().getName());
		assertEquals("Plaza Flores, Rivera", guardado.getLocation().getAddress());
    }
}
