package com.sbact1.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }


    // getters / setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User u) { this.user = u; }
    public Event getEvent() { return event; }
    public void setEvent(Event e) { this.event = e; }
    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason r) { this.reason = r; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}

