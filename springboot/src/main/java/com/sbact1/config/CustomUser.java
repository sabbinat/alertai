package com.sbact1.config;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sbact1.model.User;

/**
 * Clase que implementa UserDetails para adaptar tu entidad User al modelo de seguridad de Spring Security.
 */
public class CustomUser implements UserDetails {

	private User user; 

	// Constructor que recibe un usuario y lo guarda internamente
	public CustomUser(User user) {
		super();
		this.user = user;
	}

	/**
	 * Devuelve la colección de roles (authorities) que tiene el usuario.
	 * Spring Security los usa para controlar el acceso según los permisos.
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Se crea una autoridad simple a partir del rol del usuario (ej: "ROLE_USER", "ROLE_ADMIN")
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
		// Se devuelve una lista que contiene esa autoridad
		return Arrays.asList(authority);
	}

	/**
	 * Devuelve la contraseña del usuario (cifrada en la base de datos).
	 */
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	/**
	 * Devuelve el nombre de usuario, que en este caso es el correo electrónico.
	 */
	@Override
	public String getUsername() {
		return user.getEmail();
	}

	/**
	 * Indica si la cuenta ha expirado. Se retorna `true`, lo que significa que siempre está activa.
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * Indica si la cuenta está bloqueada. `true` indica que no está bloqueada.
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * Indica si las credenciales (contraseña) han expirado. `true` indica que son válidas.
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * Indica si la cuenta está habilitada. `true` significa que el usuario puede autenticarse.
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}
}
