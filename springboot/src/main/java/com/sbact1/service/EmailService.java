package com.sbact1.service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Servicio para el envío de correos electrónicos utilizando JavaMailSender.
 * 
 * Permite enviar correos electrónicos personalizados, incluyendo opciones para
 * establecer el remitente, destinatario, asunto, contenido, dirección de respuesta (reply-to)
 * y nombre visible del remitente. Además, proporciona métodos específicos para enviar
 * consultas de usuarios al correo institucional de soporte.
 * 
 * Las credenciales del remitente (email y contraseña) se obtienen desde el archivo
 * de configuración application-secret.properties
 * 
 */
@Service
public class EmailService {

    // Obtiene el email del remitente desde application.properties 
    @Value("${email.sender}")
    private String emailUser;

    @Value("${password.sender}")
    private String password;

    @Autowired private JavaMailSender mailSender;


    /**
     * Método que envía un correo electrónico 
     *
     * @param toUser  destinatario del correo
     * @param subject asunto del correo
     * @param content contenido del correo 
     * @throws MessagingException si ocurre un error al crear o enviar el mensaje
     */
    public void enviarCorreo(String toUser, String subject, String content, String replyTo, String displayName) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());

        // Si se proporciona un displayName (ej: nombre del usuario), se usa como nombre visible
        if (displayName != null && !displayName.isBlank()) {
            helper.setFrom(new InternetAddress(emailUser, displayName));
        } else {
            helper.setFrom(emailUser); 
        }

        helper.setTo(toUser);
        helper.setSubject(subject);
        helper.setText(content, true);

        if (replyTo != null && !replyTo.isBlank()) {
            helper.setReplyTo(replyTo);
        }

        mailSender.send(mensaje);
    }

    // Sobrecarga común para uso institucional (sin replyTo, sin displayName)
    public void enviarCorreo(String toUser, String subject, String content) throws MessagingException, UnsupportedEncodingException {
        enviarCorreo(toUser, subject, content, null, null);
    }

    // Sobrecarga para uso con replyTo (pero sin nombre visible)
    public void enviarCorreo(String toUser, String subject, String content, String replyTo) throws MessagingException, UnsupportedEncodingException {
        enviarCorreo(toUser, subject, content, replyTo, null);
    }



    /**
     * Envía una duda o mensaje de contacto de un usuario al correo institucional.
     *
     * @param name nombre del usuario que consulta
     * @param email email del usuario
     * @param asunto asunto del mensaje
     * @param message mensaje o duda que el usuario quiere enviar
     * @throws MessagingException si ocurre un error durante el envío
     */
    public void enviarDudaUsuario(String name, String email, String asunto, String message)
            throws MessagingException, UnsupportedEncodingException {

        String asuntoFinal = "Consulta de " + email + " - " + asunto;

        String contenido = """
            <div style="font-family: Arial, sans-serif;">
                <p style="white-space: pre-line;">%s</p>
            </div>
        """.formatted(message);

        // Envía al correo institucional (emailUser), con reply-to del usuario
        enviarCorreo(emailUser, asuntoFinal, contenido, email, name);
    }




}
