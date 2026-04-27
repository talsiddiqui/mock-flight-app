package com.flightapp.demo.repository;

import com.flightapp.demo.model.Flight;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByFlDate(LocalDate flDate);
}