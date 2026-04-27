package com.flightapp.demo.repository;

import com.flightapp.demo.model.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {}