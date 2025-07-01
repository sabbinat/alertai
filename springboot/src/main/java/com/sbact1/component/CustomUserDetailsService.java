package com.sbact1.component;

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
	* Carga los datos del usuario según el nombre de usuario (correo electrónico) proporcionado.
	*
	* Este método intenta recuperar una entidad {@link User} de la base de datos utilizando el nombre de usuario proporcionado,
	* que se espera que sea la dirección de correo electrónico del usuario. Si se encuentra el usuario, envuelve la entidad de usuario
	* en un objeto {@link CustomUser} que implementa {@link UserDetails} y lo devuelve.
	* Si no se encuentra el usuario, se lanza una excepción {@link UsernameNotFoundException}.
	* 
	*
	* @param username: el nombre de usuario que identifica al usuario cuyos datos se requieren (normalmente, el correo electrónico del usuario).
	* @return: un objeto {@link UserDetails} completamente rellenado (como un {@link CustomUser}).
	* @throws: UsernameNotFoundException si no se pudo encontrar al usuario.
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
