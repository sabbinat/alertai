package com.sbact1.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity 
@Setter 
@Getter 
public class Token {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne // Muchos tokens pueden pertenecer a un mismo usuario
    @JoinColumn(nullable = false, name = "user_id") // Foreign key obligatoria
    @OnDelete(action = OnDeleteAction.CASCADE) // Si el usuario es eliminado, también lo serán sus tokens
    private User user;

    // Fecha y hora de expiración del token
    private LocalDateTime expiryDate;

    // Finalidad del token: puede ser "verify" (verificación) o "reset" (restablecer contraseña)
    private String purpose;
}
