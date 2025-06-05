package com.sbact1.config;

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
	 * Este método se ejecuta automáticamente después de un inicio de sesión exitoso.
	 * Redirige al usuario a diferentes páginas según su rol.
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		// Obtiene los roles del usuario autenticado
		Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

		// Redirecciona a /admin/home si el usuario tiene el rol ADMIN
		if (roles.contains("ROLE_ADMIN")) {
			response.sendRedirect("/admin/home");
		} else {
			// Si no, lo redirige a la página de usuario
			response.sendRedirect("/user/home");
		}
	}
}
