package com.sbact1.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sbact1.model.User;
import com.sbact1.model.VerificationToken;
import com.sbact1.repository.UserRepository;
import com.sbact1.repository.VerificationTokenRepository;
import com.sbact1.service.EmailService;
import com.sbact1.service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
    
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private VerificationTokenRepository verificationTokenRepository;
    @Autowired private EmailService emailService;

    @ModelAttribute
    public void commonUser(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            User user = userRepository.findByEmail(email);
            m.addAttribute("user", user);
        }
    }

    // Método para registrar un nuevo usuario
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    // Método para manejar el inicio de sesión
    @GetMapping("/signin")
    public String login() {
        return "login";
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

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(vt);

        String link = "http://localhost:8080/confirmar?token=" + token;
        emailService.enviarCorreo(user.getEmail(), "Verifica tu cuenta", 
            "Haz clic aquí para verificar tu cuenta: <a href='" + link + "'>Verificar</a>");


        return "redirect:/user/user_home";
    }

    // Método para manejar la verificación de la cuenta
    @GetMapping("/confirmar")
    public String confirmarCuenta(@RequestParam("token") String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token);
        if (vt == null || vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "token_invalido"; 
        }
    
        User user = vt.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(vt);
    
        return "verificado_exitoso"; 
    }
    
    // Método para manejar la página de ayuda
    @GetMapping("/help")
    public String help() {
        return "help"; 
    }

    // Método para manejar la página de contacto
    @GetMapping("/contact")
    public String contact() {
        return "contact"; 
    }

    
}
