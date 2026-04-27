package com.flightapp.demo.repository;

import com.flightapp.demo.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, String> {}