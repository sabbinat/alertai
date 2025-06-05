package com.sbact1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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


}

