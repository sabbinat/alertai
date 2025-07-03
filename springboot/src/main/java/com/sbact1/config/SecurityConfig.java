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

import com.sbact1.component.CustomAuthSucessHandler;
import com.sbact1.component.CustomUserDetailsService;
import com.sbact1.repository.UserRepository;

/**
* Configuración de seguridad para la aplicación.
* 
* Esta clase define la configuración de seguridad utilizando Spring Security,
* incluyendo la gestión de autenticación, autorización, codificación de contraseñas,
* y el manejo de inicio/cierre de sesión personalizado.
*
*  Configura un codificador de contraseñas {@link BCryptPasswordEncoder} para proteger las contraseñas de los usuarios.
*  Define un {@link UserDetailsService} personalizado para cargar los detalles de usuario desde la base de datos.
*  Establece un {@link DaoAuthenticationProvider} que utiliza el servicio de usuarios y el codificador de contraseñas configurados.
*  Configura las reglas de acceso a rutas según los roles de usuario (ADMIN, USER) y la autenticación.
*  Personaliza el formulario de inicio de sesión y el comportamiento de éxito o fallo en la autenticación.
*  Gestiona el cierre de sesión y redirecciones asociadas.
* 
* Las rutas administrativas requieren el rol ADMIN, mientras que las rutas de usuario pueden ser accedidas por usuarios con los roles USER o ADMIN.
* Algunas rutas requieren autenticación, y otras están abiertas a todos los usuarios.
* 
*/
@Configuration 
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	public CustomAuthSucessHandler sucessHandler; 
    @Autowired public UserRepository userRepository;
   
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
    
    @Bean
    UserDetailsService getDetailsService() {
		return new CustomUserDetailsService();
	}

    @Bean
    DaoAuthenticationProvider getAuthenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(getDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/user/enviarConsulta").permitAll()
                .requestMatchers("/user/eliminar/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/event/save", "/event/update", "/event/delete").authenticated()
                .requestMatchers("/**").permitAll()
            )
            .formLogin(login -> login
                .loginPage("/signin") 
                .loginProcessingUrl("/userLogin") 
                .successHandler(sucessHandler) 
                .failureUrl("/signin?error=true")
                .permitAll() 
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/signin?logout=true")
                .permitAll()
            );

        return http.build(); 
    }

    
}
