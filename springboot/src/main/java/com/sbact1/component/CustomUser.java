package com.sbact1.component;

import java.util.Arrays;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sbact1.model.User;

/**
 * CustomUser es una implementación de la interfaz {@link org.springframework.security.core.userdetails.UserDetails}
 * que adapta la entidad {@code User} de la aplicación para integrarse con el sistema de autenticación de Spring Security.
 * 
 * Esta clase encapsula un objeto {@code User} y expone sus propiedades relevantes (correo electrónico, contraseña, rol, etc.)
 * a través de los métodos definidos por la interfaz {@code UserDetails}. 
 * Permite que Spring Security gestione la autenticación y autorización de usuarios utilizando la información almacenada en la entidad {@code User}.
 *
 * Funcionalidades principales:
 * 
 *   Proporciona las autoridades (roles) del usuario para el control de acceso.
 *   Expone el correo electrónico como nombre de usuario.
 *   Permite a Spring Security verificar el estado de la cuenta (habilitada, bloqueada, expirada, etc.).
 */
public class CustomUser implements UserDetails {

	private User user; 
	
	/**
	* Construye una nueva instancia de CustomUser encapsulando el objeto Usuario proporcionado.
	*
	* @param user El objeto Usuario que será encapsulado por este CustomUser.
	*/
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
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
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
		return user.isEnabled();
	}
}
