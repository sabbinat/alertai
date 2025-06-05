package com.sbact1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Clase de configuración de seguridad de Spring Security.
 */
@Configuration 
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	public CustomAuthSucessHandler sucessHandler; // Manejador personalizado que se ejecuta después de un login exitoso

    /**
     * Se utiliza para cifrar las contraseñas antes de compararlas con la base de datos.
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    /**
     * Es responsable de cargar los detalles del usuario desde la base de datos.
     */
    @Bean
    UserDetailsService getDetailsService() {
		return new CustomUserDetailsService();
	}

    /**
     * Usa el servicio de usuarios y el codificador de contraseñas configurados anteriormente.
     */
    @Bean
    DaoAuthenticationProvider getAuthenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(getDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

    /**
     * Define las reglas de acceso y el comportamiento del login.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Desactiva CSRF para facilitar pruebas 
            .authorizeHttpRequests(requests -> requests
                // Usuarios con rol ADMIN pueden eliminar usuarios
                .requestMatchers("/user/eliminar/**").authenticated()
                // Acceso a rutas administrativas solo para ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Acceso a rutas de usuario tanto para USER como para ADMIN
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                // Solo usuarios autenticados pueden guardar, actualizar o eliminar eventos
                .requestMatchers("/event/save", "/event/update", "/event/delete").authenticated()
                // Permitir acceso público a todas las demás rutas
                .requestMatchers("/**").permitAll()
            )
            // Configuración de login con formulario personalizado
            .formLogin(login -> login
                .loginPage("/signin") // Página personalizada de inicio de sesión
                .loginProcessingUrl("/userLogin") // URL para procesar el login
                .successHandler(sucessHandler) // Manejador de éxito personalizado
                .failureUrl("/signin?error=true")
                .permitAll() // Permitir acceso a la página de login para todos
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/signin?logout=true")
                .permitAll()
            );

        return http.build(); 
    }
}
