package com.becoder.user;

import com.sbact1.controller.UserController;
import com.sbact1.model.User;
import com.sbact1.repository.UserRepository;
import com.sbact1.service.EmailService;
import com.sbact1.service.UserService;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private Principal principal;

    @Mock
    private MultipartFile imageFile;

    @Mock
    private RedirectAttributes redirectAttributes;


    @Test
    void testRegistrarUsuario_emailYaExiste() {
        String email = "existente@correo.com";
        Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);

        User user = new User();
        user.setEmail(email);
        user.setPassword("123");

        assertThrows(IllegalStateException.class, () -> {
            userService.saveUser(user);
        });
    }


    @Test
    void actualizarPerfil_usuarioComun_redirigeAProfile() {
        User updatedUser = new User();
        updatedUser.setName("Nuevo nombre");

        User existingUser = new User();
        existingUser.setEmail("usuario@correo.com");
        existingUser.setRole("ROLE_USER");

        when(principal.getName()).thenReturn("usuario@correo.com");
        when(userRepository.findByEmail("usuario@correo.com")).thenReturn(Optional.of(existingUser));

        String result = userController.updateProfile(updatedUser, imageFile, principal);

        verify(userService).updateUserProfile(updatedUser, imageFile, "usuario@correo.com");
        assertEquals("redirect:/user/profile", result);
    }

    @Test
    void actualizarPerfil_admin_redirigeAHomeAdmin() {
        User updatedUser = new User();

        User existingUser = new User();
        existingUser.setEmail("admin@correo.com");
        existingUser.setRole("ROLE_ADMIN");

        when(principal.getName()).thenReturn("admin@correo.com");
        when(userRepository.findByEmail("admin@correo.com")).thenReturn(Optional.of(existingUser));

        String result = userController.updateProfile(updatedUser, null, principal);

        verify(userService).updateUserProfile(updatedUser, null, "admin@correo.com");
        assertEquals("redirect:/admin/home", result);
    }

    @Test
    void actualizarPerfil_usuarioNoExiste_redirigeASignin() {
        User updatedUser = new User();

        when(principal.getName()).thenReturn("noexiste@correo.com");
        when(userRepository.findByEmail("noexiste@correo.com")).thenReturn(Optional.empty());

        String result = userController.updateProfile(updatedUser, null, principal);

        verify(userService).updateUserProfile(updatedUser, null, "noexiste@correo.com");
        assertEquals("redirect:/signin", result);
    }

    @Test
    void eliminarUsuario_existente_sinEventos_redirigeASignin() {
        doNothing().when(userService).eliminarUsuario(1);

        String result = userController.eliminarUsuario(1, redirectAttributes);

        verify(userService).eliminarUsuario(1);
        verify(redirectAttributes).addFlashAttribute("sucessoEliminar", "Usuario eliminado exitosamente!");
        assertEquals("redirect:/signin", result);
    }

    @Test
    void eliminarUsuario_inexistente_redirigeASignin() {
        doThrow(new EntityNotFoundException()).when(userService).eliminarUsuario(99);

        String result = userController.eliminarUsuario(99, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("errorEliminar", "Usuario no encontrado");
        assertEquals("redirect:/signin", result);
    }

    @Test
    void eliminarUsuario_conEventos_redirigeAProfileConError() {
        doThrow(new IllegalStateException("No se puede eliminar usuario con eventos o comentarios asociados"))
            .when(userService).eliminarUsuario(2);

        String result = userController.eliminarUsuario(2, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("errorEliminar", "No se puede eliminar usuario con eventos o comentarios asociados");
        assertEquals("redirect:/user/profile", result);
    }

}
