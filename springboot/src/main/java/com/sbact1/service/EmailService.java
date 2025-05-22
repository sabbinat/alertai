package com.sbact1.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Value("${email.sender}")
    private String emailUser;

    @Value("${password.sender}")
    private String password;

    @Autowired
    private JavaMailSender mailSender;
 

    //  NOTIFICA CUANDO UN EVENTO DE X CATEGORÍA SE PUBLICA
    public void sendEmail(String toUser, String subject, String message) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(emailUser);
        mensaje.setTo(toUser);
        mensaje.setSubject(subject);
        mensaje.setText(message);

        mailSender.send(mensaje);
    }

    //  ENVÍA NOTIFICACIÓN DE VERIFICACIÓN DE CUENTA 
    public void enviarCorreo(String toUser, String subject, String content) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            helper.setFrom(emailUser);
            helper.setTo(toUser);
            helper.setSubject(subject);
            helper.setText(content, true); 

            mailSender.send(mensaje);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
