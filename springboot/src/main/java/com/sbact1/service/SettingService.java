package com.sbact1.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Clase de servicio para gestionar la configuración de la aplicación relacionada con las preferencias de idioma y redirección.
 * Proporciona métodos para cargar configuraciones en el modelo para la renderización de vistas y para manejar el guardado de preferencias del usuario.
 */

@Service
public class SettingService {

    public void cargarSettings(Model model, String redirectTo) {
        model.addAttribute("lang", "es");
        model.addAttribute("redirectTo", redirectTo);
    }

    public String guardarSettings(String language, String redirectTo, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Preferencias guardadas correctamente.");
        return "redirect:" + redirectTo;
    }
}
