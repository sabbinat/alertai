package com.sbact1.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sbact1.model.User;
import com.sbact1.model.Token;
import com.sbact1.repository.UserRepository;
import com.sbact1.repository.TokenRepository;
import com.sbact1.service.EmailService;
import com.sbact1.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

/** 
* AuthController maneja la autenticación del usuario, el registro, la verificación de correo electrónico, 
* Recuperación de contraseña y puntos finales relacionados con la gestión de la cuenta de usuario. 
* 
* Funcionalidades principales: 
* 
*   Registro de usuarios con verificación por correo electrónico
*   Iniciar sesión de usuario 
*   Restablecimiento de contraseña por correo electrónico Token 
*   Inyección de datos de usuario común para vistas 
* 
* Seguridad: 
* 
*   Todas las operaciones confidenciales (registro, restablecimiento de contraseña) Use tokens de tiempo limitado 
*   Las contraseñas están encriptadas antes del almacenamiento 
* 
*/
@Controller
public class AuthController {
    
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private TokenRepository tokenRepository;
    @Autowired private UserService userService;

    @ModelAttribute
    public void commonUser(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            Optional<User> user = userRepository.findByEmail(email);
            m.addAttribute("user", user);
        }
    }

    // Método para registrar un nuevo usuario
    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result, HttpSession session) {

        // Verifica si el correo ya está en uso (esto debe ir primero)
        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "", "Ya existe una cuenta con este correo");
            return "auth/register";
        }

        // Validación de errores
        if (result.hasErrors()) {
            return "auth/register";
        }

        // Guarda usuario
        User savedUser = userService.saveUser(user); // Aquí ya se codifica la contraseña y se asigna el rol

        // Genera token de verificación
        String token = UUID.randomUUID().toString();
        Token verificationToken = new Token();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationToken.setPurpose("verify");
        tokenRepository.save(verificationToken);

        // Envia correo con el enlace de verificación
        String link = "http://localhost:8080/confirmar?token=" + token;

        // Contenido HTML del correo
        String content = """
            <div style='font-family: Arial, sans-serif;'>
                <h2>¡Bienvenido a AlertAi, %s!</h2>
                <p>Gracias por registrarte. Para activar tu cuenta, por favor haz clic en el siguiente enlace:</p>
                <p style='margin: 16px 0;'>
                    <a href='%s' style='background-color:rgb(61, 87, 187); color: white; padding: 10px 20px;
                    text-decoration: none; border-radius: 5px;'>Verificar cuenta</a>
                </p>
                <p>Este enlace expirará en 24 horas por seguridad.</p>
                <p>Si no te registraste en nuestra plataforma, puedes ignorar este mensaje.</p>
                <p style='margin-top: 30px;'>Atentamente,<br>El equipo de Star Software</p>
            </div>
        """.formatted(user.getName(), link);
        
        try {
            emailService.enviarCorreo(savedUser.getEmail(), "Verifica tu cuenta en AlertAi", content);
        } catch (Exception e) {
            session.setAttribute("msg", "Hubo un problema al enviar el correo de verificación. Intenta nuevamente.");
            return "auth/register";
        }

        session.setAttribute("msg", "Registro exitoso. Te enviamos un correo para verificar tu cuenta.");
        return "auth/successful_registration";
    }

    // Método para manejar la verificación de la cuenta
    @GetMapping("/confirmar")
    public String confirmAccount(@RequestParam("token") String token) {
        Optional<Token> tk = tokenRepository.findByToken(token);

        if (tk.isEmpty() || 
            tk.get().getExpiryDate().isBefore(LocalDateTime.now()) ||
            !"verify".equals(tk.get().getPurpose())) {

            return "auth/token_invalido";
        }

        User user = tk.get().getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(tk.get());

        return "auth/verificado_exitoso";
    }


    // Página de registro exitoso
    @GetMapping("/successful_registration")
    public String registerExited() {
        return "auth/successful_registration"; 
    }
    
    
    // Método que maneja el login
    @GetMapping("/signin")
    public String login() {
        return "auth/login";
    }


    // Método para recuperar contraseña
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        return "auth/forgot-password";
    }

    @Transactional
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes msg) {

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            msg.addFlashAttribute("error", "No existe una cuenta con ese correo.");
            return "redirect:/forgot-password";
        }

        User user = optionalUser.get();

        tokenRepository.deleteByUserId(user.getId());

        // Crea un token para el reset
        String token = UUID.randomUUID().toString();
        Token resetToken = new Token();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        resetToken.setPurpose("reset"); 
        tokenRepository.save(resetToken);

        // Envia correo con link
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        String subject = "Restablece tu contraseña";
        String content = """
            <div style="font-family: Arial, sans-serif;">
                <p>Hola <strong>%s</strong>,</p>
                <p>Recibimos una solicitud para restablecer tu contraseña.</p>
                <p>Para continuar, debes hacer clic en el siguiente enlace:</p>
                <p>
                    <a href="%s" style="display: inline-block; color: #007bff; text-decoration: none;">
                        Restablecer contraseña
                    </a>
                </p>
                <p style="margin-top: 20px;">Si no solicitaste este cambio, puedes ignorar este mensaje. Tu cuenta seguirá segura.</p>
                <hr>
                <p style="font-size: 0.9em; color: #888;">Este enlace estará disponible por 30 minutos.</p>
            </div>
            """.formatted(user.getName(), resetUrl);

        try {
            emailService.enviarCorreo(user.getEmail(), subject, content);
        } catch (Exception e) {
            msg.addFlashAttribute("error", "No se pudo enviar el correo. Intenta nuevamente más tarde.");
            return "redirect:/forgot-password";
        }

        msg.addFlashAttribute("success", "Te enviamos un correo con instrucciones.");
        return "redirect:/forgot-password";
    }


    // Método para crear nueva contraseña
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Optional<Token> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty() || 
            optionalToken.get().getExpiryDate().isBefore(LocalDateTime.now()) || 
            !"reset".equals(optionalToken.get().getPurpose())) {

            model.addAttribute("error", "El enlace no es válido o ha expirado.");
            return "auth/reset-password";
        }

        model.addAttribute("token", token);
        return "auth/reset-password";

    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token, @RequestParam("password") String newPassword,
            RedirectAttributes redirectAttributes) {

        Optional<Token> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty() || 
            optionalToken.get().getExpiryDate().isBefore(LocalDateTime.now()) ||
            !"reset".equals(optionalToken.get().getPurpose())) {

            redirectAttributes.addFlashAttribute("error", "Token inválido o expirado.");
            return "redirect:/reset-password?token=" + token;
        }

        User user = optionalToken.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword)); 
        userRepository.save(user);

        tokenRepository.delete(optionalToken.get()); 

        redirectAttributes.addFlashAttribute("success", "Contraseña restablecida con éxito.");
        return "redirect:/signin";
    }


}
