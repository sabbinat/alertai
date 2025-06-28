package com.sbact1.component;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthSucessHandler implements AuthenticationSuccessHandler {

	/**
	* Gestiona eventos de autenticación exitosa.
	* 
	* Este método se invoca cuando un usuario se ha autenticado correctamente.
	* Recupera los roles del usuario autenticado y los redirige según su rol:
	* 
	* Si el usuario tiene la autorización "ROLE_ADMIN", se le redirige a /admin/home.
	* De lo contrario, se le redirige a /user/home.
	* 
	*
	* @param solicita el objeto {@link HttpServletRequest}
	* @param responde el objeto {@link HttpServletResponse}
	* @param authentication el objeto {@link Authentication} que contiene la información de autenticación del usuario
	* @throws IOException si se produce un error de entrada o salida durante la redirección
	* @throws ServletException si se produce un error específico del servlet
	*/
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, 
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

		if (roles.contains("ROLE_ADMIN")) {
			response.sendRedirect("/admin/home");
		} else {
			response.sendRedirect("/user/home");
		}
	}
}
