package com.sbact1.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token único que se utiliza para verificar al usuario, por ejemplo para confirmación de cuenta o recuperación de contraseña
    private String token;

    @OneToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    // Fecha y hora de expiración del token, después de la cual el token ya no es válido
    private LocalDateTime expiryDate;

    private String purpose; // "verify" o "reset"

}

