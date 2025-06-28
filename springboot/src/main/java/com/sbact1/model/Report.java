package com.sbact1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Report {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private User user;

    @ManyToOne(optional=false)
    private Event event;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    private LocalDateTime createdAt;

    private boolean reviewed = false;
    private boolean deleted = false;  
    private boolean viewed= false; 

    @Transient
    public String getTimeAgo() {
        if (createdAt == null) return "";

        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        long seconds = duration.getSeconds();
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

