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
import jakarta.validation.Valid;

@Controller
public class HomeController {
    
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailService emailService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private TokenRepository tokenRepository;

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

    @GetMapping("/successful_registration")
    public String registroExitoso() {
        return "auth/successful_registration"; 
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute @Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "auth/register";
        }

        // Verificar si el correo ya está en uso (opcional pero recomendado)
        if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "", "Ya existe una cuenta con este correo");
            return "auth/register";
        }

        // Encriptar la contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return "auth/successful_registration";
    }

    // Método para manejar el inicio de sesión
    @GetMapping("/signin")
    public String login() {
        return "auth/login";
    }

    // Método para guardar el usuario
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute User user, HttpSession session, Model m) {
        User u = userService.saveUser(user);

        if (u != null) {
            session.setAttribute("msg", "Registrado con éxito");
        } else {
            session.setAttribute("msg", "Algo salió mal");
        }

        String token = UUID.randomUUID().toString();

        Token vt = new Token();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(vt);

        String link = "http://localhost:8080/confirmar?token=" + token;
        try {
            emailService.enviarCorreo(user.getEmail(), "Verifica tu cuenta", 
                "Haz clic aquí para verificar tu cuenta: <a href='" + link + "'>Verificar</a>");
        } catch (Exception e) {
            session.setAttribute("msg", "Hubo un problema al enviar el correo de verificación.");
            return "auth/register"; 
        }

        return "redirect:/user/user_home";
    }

    // Método para manejar la verificación de la cuenta
    @GetMapping("/confirmar")
    public String confirmarCuenta(@RequestParam("token") String token) {
        Optional<Token> tk = tokenRepository.findByToken(token);

        // Validar si el token está presente y no expiró
        if (tk.isEmpty() || tk.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return "auth/token_invalido";
        }

        User user = tk.get().getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(tk.get());

        return "auth/verificado_exitoso";
    }

    // Método para manejar la página de ayuda
    @GetMapping("/help")
    public String help(Model model, Principal principal) {
        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }
        return "general/help"; 
    }

    // Método para manejar la página de contacto
    @GetMapping("/contact")
    public String contact(Model model, Principal principal) {
        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }
        return "general/contact"; 
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No existe una cuenta con ese correo.");
            return "redirect:/forgot-password";
        }

        User user = optionalUser.get();

        // Crear token
        String token = UUID.randomUUID().toString();
        Token resetToken = new Token();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        resetToken.setPurpose("reset"); 
        tokenRepository.save(resetToken);

        // Enviar correo con link
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;
        String subject = "Recuperar contraseña";
        String content = "<p>Hola " + user.getName() + ",</p>"
            + "<p>Haz clic en el siguiente enlace para restablecer tu contraseña:</p>"
            + "<a href=\"" + resetUrl + "\">Restablecer contraseña</a>";

        try {
            emailService.enviarCorreo(user.getEmail(), subject, content);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo enviar el correo. Intenta más tarde.");
            return "redirect:/forgot-password";
        }


        redirectAttributes.addFlashAttribute("success", "Te enviamos un correo con instrucciones.");
        return "redirect:/forgot-password";
    }

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
