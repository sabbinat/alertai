package com.sbact1.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.web.multipart.MultipartFile;

import com.sbact1.model.Event;
import com.sbact1.model.Location;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime time;
    private String description;
    private Long categoryId;
    private Location location = new Location(); 
    private String contact;
    private MultipartFile imageFile;
    private String image;
    private LocalDateTime registrationTime;
    private String timeAgo;
    private String formattedStartDate;
    private String formattedEndDate;


    public EventDto() {

    }

    public EventDto(Event event, DateTimeFormatter formatter) {
        this.id = event.getId();
        this.name = event.getName();
        this.startDate = event.getStartDate();
        this.endDate = event.getEndDate();
        this.time = event.getTime();
        this.description = event.getDescription();
        this.categoryId = event.getCategory().getId();
        this.location = event.getLocation();
        this.contact = event.getContact();
        this.image = event.getImage();
        this.registrationTime = event.getRegistrationTime();
        this.timeAgo = event.getTimeAgo();
        this.formattedStartDate = event.getStartDate().format(formatter.withLocale(Locale.forLanguageTag("es-ES")));
        this.formattedEndDate = event.getEndDate().format(formatter.withLocale(Locale.forLanguageTag("es-ES")));
    }
}
