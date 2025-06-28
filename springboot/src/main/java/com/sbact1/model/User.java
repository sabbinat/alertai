package com.sbact1.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Transient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(nullable = false) //campo obligatorio
	private boolean enabled = false;

	@NotBlank 
	private String name;

	@NotBlank
	@Email
	@Column(unique = true)
	private String email;


	@Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,}$",
        message = "La contraseña debe contener una mayúscula, una minúscula, un número y un símbolo especial"
    )
	
	private String password; //contraseña con validacion de seguridad 

	@Column(nullable = true)
	private String phone; 
	
	private boolean phoneVerified = false;

	private String country;
	private String document;

	private String role; // ROLE_USER O ROLE_ADMIN

	@Column(nullable = true)
	private String image;  

	private String description;

	@Column(nullable = true, updatable = false)
	private LocalDateTime registrationTime;  

	@NotBlank
	@Column(unique = true, nullable = false)
	private String username;

	@ManyToMany // Relación muchos a muchos con notificación de categorias 
    @JoinTable(
        name = "user_category",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> notificaciones;

	// Comentarios que ha hecho el usuario
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments;

	// Tokens asosiados 
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Token> tokens;

	@PrePersist
	public void prePersist() {
		this.registrationTime = LocalDateTime.now();  
	}

	@Transient 
	public String getTimeAgo() {
		if (registrationTime == null) return "";

		Duration duration = Duration.between(registrationTime, LocalDateTime.now());

		long seconds = duration.toSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60)
            return "hace " + seconds + (seconds == 1 ? " segundo" : " segundos");
        else if (minutes < 60)
            return "hace " + minutes + (minutes == 1 ? " minuto" : " minutos");
        else if (hours < 24)
            return "hace " + hours + (hours == 1 ? " hora" : " horas");
        else if (days < 7)
            return "hace " + days + (days == 1 ? " día" : " días");
        else if (days < 30) {
            long weeks = days / 7;
            return "hace " + weeks + (weeks == 1 ? " semana" : " semanas");
        } else if (days < 365) {
            long months = days / 30;
            return "hace " + months + (months == 1 ? " mes" : " meses");
        } else {
            long years = days / 365;
            return "hace " + years + (years == 1 ? " año" : " años");
        }
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", registrationTime=" + registrationTime + "]";
	}
}
