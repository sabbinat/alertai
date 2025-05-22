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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	public CustomAuthSucessHandler sucessHandler;

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
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
      http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(requests -> requests
              .requestMatchers("/user/eliminar/**").hasRole("ADMIN") 
              .requestMatchers("/admin/**").hasRole("ADMIN")
              .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // 👈 clave
              .requestMatchers("/event/save", "/event/update", "/event/delete").authenticated()  
              .requestMatchers("/**").permitAll()
          )
          .formLogin(login -> login
              .loginPage("/signin")
              .loginProcessingUrl("/userLogin")
              .successHandler(sucessHandler)
              .permitAll()
          );
  
      return http.build();
  }
  

}
