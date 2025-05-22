package com.sbact1.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sbact1.model.Location;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EventDto {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime time;
    private String description;
    private Long categoryId; 
    private Location location;
    private String contact;
    

    
}

