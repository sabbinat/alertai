package com.sbact1.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.sbact1.model.User;
import com.sbact1.repository.UserRepository;

import java.util.Optional;
import java.util.logging.Logger;

@Component
@Profile("!test")
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(AdminInitializer.class.getName());

    @Autowired
    private UserRepository userRepository;

    /**
     * 
     * Se ejecuta al iniciar la aplicación para garantizar la existencia de un usuario administrador predeterminado.
     * 
     * Este método comprueba si un usuario con el correo electrónico "admin@gmail.com" está presente en la base de datos.
     * De lo contrario, crea un nuevo usuario administrador con credenciales predefinidas y lo guarda.
     * Si el administrador ya existe, registra que no se creó ningún usuario nuevo.
     * 
     *
     * @param args: argumentos de la línea de comandos pasados ​​a la aplicación.
     */
    @Override
    public void run(String... args) {
        Optional<User> existingAdmin = userRepository.findByEmail("admin@gmail.com");

        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setName("Administrador");
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("admin@123"));
            admin.setRole("ROLE_ADMIN");
            admin.setEnabled(true);
            admin.setPhoneVerified(true);
            admin.setCountry("Sistema");
            admin.setDescription("Usuario administrador generado automáticamente.");

            userRepository.save(admin);
            logger.info("Administrador creado por defecto con email: admin@gmail.com y contraseña: admin@123");
        } else {
            logger.info("Usuario administrador ya existe, no se creó uno nuevo.");
        }
    }
}

