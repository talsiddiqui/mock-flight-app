package com.flightapp.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "airlines")
public class Airline {
    @Id
    @Column(name = "airline_code", length = 10)
    private String airlineCode;
    
    @Column(name = "airline", columnDefinition = "TEXT")
    private String airline;
    
    @Column(name = "airline_dot", columnDefinition = "TEXT")
    private String airlineDot;
    
    @Column(name = "dot_code")
    private Integer dotCode;
}