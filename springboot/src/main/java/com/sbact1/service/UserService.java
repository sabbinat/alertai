package com.sbact1.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.sbact1.model.User;
import com.sbact1.repository.UserRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.TokenRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

/**
 * Servicio para la gestión de usuarios en la aplicación.
 * Proporciona métodos para registrar, actualizar, eliminar usuarios y gestionar sus perfiles.
 * También maneja la codificación de contraseñas, verificación de email y teléfono, 
 * y la gestión de imágenes de perfil.
 *
 * Funcionalidades principales:
 * 
 *   Registrar un nuevo usuario asegurando la unicidad del email y codificando la contraseña.
 *   Obtener un usuario por su email.
 *   Remover mensajes de sesión (por ejemplo, mensajes flash).
 *   Actualizar el perfil del usuario, incluyendo la imagen de perfil y verificación de teléfono.
 *   Eliminar un usuario de forma transaccional, eliminando también sus tokens y eventos asociados.
 *   Cambiar la contraseña del usuario validando la contraseña actual.
 *
 * Excepciones:
 * 
 *   Lanza {@link IllegalStateException} si el email ya está en uso al registrar un usuario.
 *   Lanza {@link UsernameNotFoundException} si no se encuentra un usuario por email.
 *   Lanza {@link RuntimeException} si ocurre un error al guardar la imagen de perfil o si el usuario no existe.
 * 
 *
 * Anotaciones:
 * 
 *   {@code @Transactional} en la eliminación de usuario para asegurar la atomicidad de la operación.
 * 
 */
@Service
public class UserService {

	@Autowired private BCryptPasswordEncoder passwordEncoder;
	@Autowired private UserRepository userRepository;
	@Autowired private TokenRepository tokenRepository;
	@Autowired private EventRepository eventRepository;

	// Método para guardar un nuevo usuario en la base de datos
	public User saveUser(User user) {
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new IllegalStateException("Email ya en uso");
		}

		// Codifica la contraseña antes de guardarla
		String password=passwordEncoder.encode(user.getPassword());
		user.setPassword(password);
		user.setRole("ROLE_USER"); 

		String username = user.getUsername();
		if (username != null && !username.startsWith("@")) {
			user.setUsername("@" + username);
		}

		User newuser = userRepository.save(user);
		
		return newuser;
	}

	public User getByEmail(String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
	}

	// Método para remover el mensaje de sesión (por ejemplo, después de mostrar un mensaje flash)
	public void removeSessionMessage() {
		var requestAttributes = RequestContextHolder.getRequestAttributes();

		if (requestAttributes != null) {
			HttpSession session = ((ServletRequestAttributes) requestAttributes)
					.getRequest()
					.getSession();
			session.removeAttribute("msg");
		}
	}

	// Método para actualizar el perfil del usuario, incluyendo su imagen
	public void updateUserProfile(User updatedUser, MultipartFile file, String email, boolean phoneVerified) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		user.setName(updatedUser.getName());
		user.setPhone(updatedUser.getPhone());
		user.setCountry(updatedUser.getCountry());
		user.setDocument(updatedUser.getDocument());
		user.setUsername(updatedUser.getUsername());
		user.setDescription(updatedUser.getDescription());
		user.setEnabled(user.isEnabled()); 

		if (phoneVerified) {
        	user.setPhoneVerified(true);
		}	
		
		if (file != null && !file.isEmpty()) {
			try {
				String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
				Path uploadPath = Paths.get("uploads/icons");
				Files.createDirectories(uploadPath);

				try (InputStream in = file.getInputStream()) {
					Files.copy(in, uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
				}
				user.setImage(filename);
			} catch (IOException e) {
				throw new RuntimeException("Error al guardar la imagen", e);
			}
		}

		userRepository.save(user);
	}

	// Método para eliminar un usuario de forma transaccional
	@Transactional
	public void deleteUser(Integer userId) {
		// Primero elimina los tokens de verificación asociados
		tokenRepository.deleteByUserId(userId); 
		// Luego elimina los eventos asociados
    	eventRepository.deleteByUserId(userId);
		//Luego elimina el usuario
		userRepository.deleteById(userId);      
	}

	public boolean cambiarPassword(String email, String currentPassword, String newPassword) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
				return false;
			}
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
			return true;
		}
		return false;
	}
}
