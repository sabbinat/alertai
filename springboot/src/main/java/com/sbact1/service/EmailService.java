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
 * - enviarCorreo: Métodos sobrecargados para enviar correos electrónicos con diferentes niveles de personalización.
 * - enviarDudaUsuario: Envía una consulta de usuario al correo institucional de soporte, incluyendo los datos del usuario y su mensaje.
 * - Excepciones: Los métodos pueden lanzar MessagingException y UnsupportedEncodingException
 * si ocurre un error durante la creación o el envío del mensaje.
 * 
 */
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



    // /**
    //  * Envia una duda o mensaje de contacto de un usuario al correo institucional.
    //  *
    //  * @param name nombre del usuario que consulta
    //  * @param email email del usuario
    //  * @param asunto asunto del mensaje
    //  * @param message mensaje o duda que el usuario quiere enviar
    //  * @throws MessagingException si ocurre un error durante el envío
    //  */
    // public void enviarDudaUsuario(String name, String email, String asunto, String message) throws MessagingException, UnsupportedEncodingException  {
    //     String asuntoFinal = "Consulta de " + name + " - " + asunto;

    //     String contenido = """
    //         <h3>📩 Nueva consulta de usuario %s</h3>
    //         <p><strong>Email:</strong> %s</p>
    //         <p><strong>Mensaje:</strong><br>%s</p>
    //     """.formatted(name, email, message);

    //     // El correo va a tu institucional, pero con replyTo del usuario
    //     enviarCorreo(emailUser, asuntoFinal, contenido, email, name);
    // }

    public void enviarDudaUsuario(String nombre, String email, String asunto, String mensaje)
        throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("zonalerta@gmail.com", "AlertaAi Soporte");
        helper.setTo("fernandeznatalie18@gmail.com");
        helper.setSubject("Nueva consulta: " + asunto);

        String contenido = "<p><strong>Nombre:</strong> " + nombre + "</p>"
                        + "<p><strong>Email:</strong> " + email + "</p>"
                        + "<p><strong>Asunto:</strong> " + asunto + "</p>"
                        + "<p><strong>Mensaje:</strong></p><p>" + mensaje + "</p>";

        helper.setText(contenido, true);
        mailSender.send(message);
    }




}
