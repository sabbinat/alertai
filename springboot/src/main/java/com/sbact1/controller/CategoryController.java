package com.sbact1.controller;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import com.sbact1.repository.CategoryRepository;
import com.sbact1.repository.EventRepository;
import com.sbact1.repository.UserRepository;

import jakarta.validation.Valid;

/**
 * Controlador para la gestión de categorías en la aplicación.
 * Proporciona endpoints para crear, editar y eliminar categorías.
 * 
 * Funcionalidades principales:
 * 
 *   Guardar una nueva categoría.
 *   Mostrar el formulario de edición para una categoría existente.
 *   Procesar la edición de una categoría.
 *   Eliminar una categoría si no tiene eventos asociados.
 * 
 * Las rutas gestionadas por este controlador están bajo el prefijo "/category".
 * 
 */
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
            msg.addFlashAttribute("error", "Error al editar la categoría");
            return "redirect:/admin/allCategories";
        }

        if (categoria.isPresent()) {
            var categoriaModel = categoria.get();
            BeanUtils.copyProperties(categoriaDto, categoriaModel);
            categoryRepository.save(categoriaModel);
            msg.addFlashAttribute("success", "¡Categoría actualizada exitosamente!");
        } else {
            msg.addFlashAttribute("error", "No se encontró la categoría.");
        }

        return "redirect:/admin/allCategories";
    }

    // Elimina una categoría si no tiene eventos asociados
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable(value = "id") Long id, RedirectAttributes msg) {
        Optional<Category> category = categoryRepository.findById(id);
        if(category.isEmpty()) {
            msg.addFlashAttribute("error", "Categoría no encontrada.");
            return "redirect:/admin/allCategories";
        }
        categoryRepository.deleteById(id);
        msg.addFlashAttribute("success", "¡Categoría eliminada exitosamente!");
        return "redirect:/admin/allCategories";
    }

    
}
