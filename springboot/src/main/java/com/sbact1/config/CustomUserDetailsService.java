package com.sbact1.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.sbact1.model.User;
import com.sbact1.repository.UserRepository;

@Component 
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository; 

	/**
	 * Método que carga los detalles del usuario a partir del correo electrónico (username).
	 * Es llamado automáticamente por Spring Security durante el proceso de autenticación.
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> optionalUser = userRepository.findByEmail(username);

		User user = optionalUser.orElseThrow(() -> 
			new UsernameNotFoundException("Usuario no encontrado")
		);

		System.out.println(user);
		return new CustomUser(user);
	}

}
