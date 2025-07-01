package com.sbact1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String contact;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.ACTIVO; 


    private LocalTime time;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = true)
    private String image;

    @Column(nullable = true, updatable = false)
	private LocalDateTime registrationTime; 

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


}

