package com.sbact1.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.sbact1.model.User;
import com.sbact1.repository.UserRepository;
import com.sbact1.repository.VerificationTokenRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired private BCryptPasswordEncoder passwordEncoder;
	@Autowired private UserRepository userRepository;
	@Autowired private VerificationTokenRepository tokenRepository;

	// Método para guardar un nuevo usuario en la base de datos
	public User saveUser(User user) {
		// Codifica la contraseña antes de guardarla
		String password=passwordEncoder.encode(user.getPassword());
		user.setPassword(password);
		user.setRole("ROLE_USER"); // asigna el rol por defecto
		User newuser = userRepository.save(user);
		
		return newuser;
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
	public void updateUserProfile(User updatedUser, MultipartFile file, String email) {
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new RuntimeException("Usuario no encontrado");
		}

		user.setName(updatedUser.getName());
		user.setPhone(updatedUser.getPhone());
		user.setCountry(updatedUser.getCountry());
		user.setDocument(updatedUser.getDocument());
		user.setUsername(updatedUser.getUsername());
		user.setDescription(updatedUser.getDescription());


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
	public void eliminarUsuario(Integer userId) {
		// Primero elimina los tokens de verificación asociados
		tokenRepository.deleteByUserId(userId); 
		//Luego elimina el usuario
		userRepository.deleteById(userId);      
	}



}
