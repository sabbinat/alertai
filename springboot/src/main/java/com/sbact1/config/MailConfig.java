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
     * Define un bean de JavaMailSender, que es el componente responsable de enviar correos electrónicos.
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Configura el servidor SMTP de Gmail
        mailSender.setHost("smtp.gmail.com");  
        mailSender.setPort(587); // Puerto para TLS (STARTTLS)
        mailSender.setUsername(emailUser);  // Correo desde el que se enviarán los mensajes
        mailSender.setPassword(password);   // Contraseña 

        // Configuración adicional para el protocolo SMTP
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");  // Protocolo usado
        props.put("mail.smtp.auth", "true");           // Habilita autenticación SMTP
        props.put("mail.smtp.starttls.enable", "true"); // Habilita STARTTLS (seguridad cifrada)
        props.put("mail.debug", "true");  // Muestra información detallada en la consola (para pruebas)

        return mailSender;
    }
}
