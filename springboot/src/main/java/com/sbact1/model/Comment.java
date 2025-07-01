package com.sbact1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private LocalDateTime date = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Comentario padre (si es respuesta)
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // Respuestas a este comentario
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> responses;

    private boolean viewed= false; 

    @Transient
    public String getTimeAgo() {
        if (date == null) return "";

        Duration duration = Duration.between(date, LocalDateTime.now());

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

