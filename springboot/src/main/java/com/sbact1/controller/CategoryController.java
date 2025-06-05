package com.sbact1.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sbact1.dto.CategoryDto;  
import com.sbact1.model.Category;
import com.sbact1.model.User;
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    UserRepository userRepository;

    // Método para guardar una nueva categoría
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute CategoryDto categoryDto) {  
        Category category = new Category();
        category.setName(categoryDto.name()); 
        categoryRepository.save(category);

        return "redirect:/admin/allCategories";
    }

    // Muestra todos los eventos que pertenecen a una categoría específica
    @GetMapping("/{id}/events")
    public String eventsByCategory(@PathVariable Long id, Model model, Principal principal) {
        List<Category> categories = categoryRepository.findAll();

        var category = categoryRepository.findById(id);
        if (category.isEmpty()) return "redirect:/category/list";

        // Agrega los datos necesarios al modelo para la vista
        model.addAttribute("categories", categories);
        model.addAttribute("category", category.get());
        model.addAttribute("events", eventRepository.findByCategoryId(id));

        // Obtener el usuario 
        if (principal != null) {
            Optional<User> optionalUser = userRepository.findByEmail(principal.getName());
            optionalUser.ifPresent(user -> model.addAttribute("user", user));
        }

        return "category/events_by_category"; 
    }

    // Muestra el formulario de edición para una categoría existente
    @GetMapping("/edit/{id}")
    public ModelAndView editar(@PathVariable(value = "id") Long id) {
        ModelAndView mv = new ModelAndView("category/edit");
        Optional<Category> categoria = categoryRepository.findById(id);
        if (categoria.isPresent()) {
            mv.addObject("id", categoria.get().getId());
            mv.addObject("name", categoria.get().getName());
        }
        return mv;
    }

    // Procesa la edición de una categoría
    @PostMapping("/edit/{id}")
    public String editarBD(@ModelAttribute @Valid CategoryDto categoriaDto,
                        BindingResult result, RedirectAttributes msg,
                        @PathVariable(value = "id") Long id) {

        Optional<Category> categoria = categoryRepository.findById(id);

        if (result.hasErrors()) {
            msg.addFlashAttribute("errorCadastrar", "Error al editar la categoría");
            return "redirect:/admin/allCategories";
        }

        if (categoria.isPresent()) {
            var categoriaModel = categoria.get();
            BeanUtils.copyProperties(categoriaDto, categoriaModel);
            categoryRepository.save(categoriaModel);
            msg.addFlashAttribute("sucessoCadastrar", "¡Categoría actualizada exitosamente!");
        } else {
            msg.addFlashAttribute("errorCadastrar", "Categoría no encontrada");
        }

        return "redirect:/admin/allCategories";
    }

    // Elimina una categoría si no tiene eventos asociados
    @GetMapping("/delete/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes msg) {
        Optional<Category> categoria = categoryRepository.findById(id);

        if (categoria.isEmpty()) {
            msg.addFlashAttribute("errorEliminar", "Categoría no encontrada");
            return "redirect:/admin/allCategories";
        }

        if (!eventRepository.findByCategoryId(id).isEmpty()) {
            msg.addFlashAttribute("errorEliminar", "No se puede eliminar: la categoría tiene eventos asociados.");
            return "redirect:/admin/allCategories";
        }

        categoryRepository.deleteById(id);
        msg.addFlashAttribute("sucessoEliminar", "Categoría eliminada exitosamente.");
        return "redirect:/admin/allCategories";
    }
    
}
