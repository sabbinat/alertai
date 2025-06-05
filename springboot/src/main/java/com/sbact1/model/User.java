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

	@Column(nullable = false)
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
	@NotBlank
	private String password;

	@Column(nullable = true)
	private String phone;  

	private String country;
	private String document;
	private String role;

	@Column(nullable = true)
	private String image;  

	private String description;

	@Column(nullable = true, updatable = false)
	private LocalDateTime registrationTime;  

	@NotBlank
	@Column(unique = true, nullable = false)
	private String username;

	@ManyToMany
    @JoinTable(
        name = "user_category",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> notificaciones;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments;

	@PrePersist
	public void prePersist() {
		this.registrationTime = LocalDateTime.now();  
	}

	@Transient
	public String getTimeAgo() {
		if (registrationTime == null) return "";

		Duration duration = Duration.between(registrationTime, LocalDateTime.now());

		long minutes = duration.toMinutes();
		long hours = duration.toHours();
		long days = duration.toDays();

		if (minutes < 60)
			return "hace " + minutes + " " + (minutes == 1 ? "minuto" : "minutos");
		else if (hours < 24)
			return "hace " + hours + " " + (hours == 1 ? "hora" : "horas");
		else if (days < 7)
			return "hace " + days + " " + (days == 1 ? "día" : "días");
		else if (days < 30) {
			long weeks = days / 7;
			return "hace " + weeks + " " + (weeks == 1 ? "semana" : "semanas");
		} else {
			long months = days / 30;
			return "hace " + months + " " + (months == 1 ? "mes" : "meses");
		}
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", password=" + password + ", registrationTime=" + registrationTime + "]";
	}
}
