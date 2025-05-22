package com.sbact1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${email.sender}")
    private String emailUser;

    @Value("${password.sender}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");  
        mailSender.setPort(587); 
        mailSender.setUsername(emailUser);  
        mailSender.setPassword(password);  

        // Configuración de propiedades SMTP
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");  
        props.put("mail.smtp.auth", "true");  
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");  

        return mailSender;
    }
}
