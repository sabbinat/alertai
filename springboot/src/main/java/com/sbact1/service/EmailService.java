package com.sbact1.service;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    // Obtiene el email del remitente desde application.properties 
    @Value("${email.sender}")
    private String emailUser;

    // Obtiene la contraseña del remitente 
    @Value("${password.sender}")
    private String password;

    // Inyecta automáticamente el componente JavaMailSender que se encarga de enviar correos
    @Autowired private JavaMailSender mailSender;

    /**
     * Método que envía un correo electrónico 
     *
     * @param toUser  destinatario del correo
     * @param subject asunto del correo
     * @param content contenido del correo 
     * @throws MessagingException si ocurre un error al crear o enviar el mensaje
     */
    public void enviarCorreo(String toUser, String subject, String content) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());

        helper.setFrom(emailUser);     // Remitente
        helper.setTo(toUser);          // Destinatario
        helper.setSubject(subject);    // Asunto
        helper.setText(content, true); 

        // Envía el mensaje por correo electrónico
        mailSender.send(mensaje);
    }

}
