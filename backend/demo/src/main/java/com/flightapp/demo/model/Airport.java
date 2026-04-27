package com.flightapp.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "airports")
public class Airport {
    @Id
    @Column(name = "airport_code", length = 3)
    private String airportCode;
    
    @Column(name = "city", nullable = false, columnDefinition = "TEXT")
    private String city;
}