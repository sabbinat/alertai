package com.sbact1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration 
public class MailConfig {

    // Inyecta el valor de la propiedad "email.sender" desde application.properties 
    @Value("${email.sender}")
    private String emailUser;

    // Inyecta la contraseña del correo desde la configuración
    @Value("${password.sender}")
    private String password;

    /**
    * Configura y proporciona un bean {@link JavaMailSender} para enviar correos electrónicos mediante el servidor SMTP de Gmail.
    * Este método configura el remitente del correo con las siguientes propiedades:
    * 
    *   Host SMTP: smtp.gmail.com
    *   Puerto: 587 (TLS/STARTTLS habilitado)
    *   Nombre de usuario y contraseña para la autenticación
    *   Habilita la autenticación SMTP y STARTTLS para una comunicación segura
    *   Habilita el modo de depuración para un registro detallado (útil durante el desarrollo)
    * 
    * @return una instancia de {@link JavaMailSender} configurada para enviar correos electrónicos.
    */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Configura el servidor SMTP de Gmail
        mailSender.setHost("smtp.gmail.com");  
        mailSender.setPort(587); 
        mailSender.setUsername(emailUser); 
        mailSender.setPassword(password);   

        // Configuración adicional para el protocolo SMTP
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");  
        props.put("mail.smtp.auth", "true");           
        props.put("mail.smtp.starttls.enable", "true"); 
        props.put("mail.debug", "true");  

        return mailSender;
    }
}
