package com.flightapp.demo.service;

import com.flightapp.demo.model.Airline;
import com.flightapp.demo.model.Airport;
import com.flightapp.demo.model.Flight;
import com.flightapp.demo.repository.AirlineRepository;
import com.flightapp.demo.repository.AirportRepository;
import com.flightapp.demo.repository.FlightRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightEtlService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;

    public FlightEtlService(FlightRepository flightRepository, AirlineRepository airlineRepository, AirportRepository airportRepository) {
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
        this.airportRepository = airportRepository;
    }

    @Transactional
    public void processCsv(MultipartFile file) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<Flight> flightsToSave = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                // 1. Process Airline (Parent)
                Airline airline = new Airline();
                airline.setAirlineCode(csvRecord.get("AIRLINE_CODE"));
                airline.setAirline(csvRecord.get("AIRLINE"));
                airline.setAirlineDot(csvRecord.get("AIRLINE_DOT"));
                airline.setDotCode(parseInteger(csvRecord.get("DOT_CODE")));
                airlineRepository.save(airline); // Save handles existence check under the hood for small datasets

                // 2. Process Airports (Parents)
                Airport origin = new Airport();
                origin.setAirportCode(csvRecord.get("ORIGIN"));
                origin.setCity(csvRecord.get("ORIGIN_CITY"));
                airportRepository.save(origin);

                Airport dest = new Airport();
                dest.setAirportCode(csvRecord.get("DEST"));
                dest.setCity(csvRecord.get("DEST_CITY"));
                airportRepository.save(dest);

                // 3. Process Flight
                Flight flight = new Flight();
                flight.setFlDate(LocalDate.parse(csvRecord.get("FL_DATE")));
                flight.setAirline(airline);
                flight.setFlNumber(parseInteger(csvRecord.get("FL_NUMBER")));
                flight.setOrigin(origin);
                flight.setDest(dest);

                // Times and Delays
                flight.setCrsDepTime(parseShort(csvRecord.get("CRS_DEP_TIME")));
                flight.setDepTime(parseShort(csvRecord.get("DEP_TIME")));
                flight.setDepDelay(parseFloat(csvRecord.get("DEP_DELAY")));
                flight.setTaxiOut(parseFloat(csvRecord.get("TAXI_OUT")));
                flight.setWheelsOff(parseShort(csvRecord.get("WHEELS_OFF")));
                flight.setWheelsOn(parseShort(csvRecord.get("WHEELS_ON")));
                flight.setTaxiIn(parseFloat(csvRecord.get("TAXI_IN")));
                flight.setCrsArrTime(parseShort(csvRecord.get("CRS_ARR_TIME")));
                flight.setArrTime(parseShort(csvRecord.get("ARR_TIME")));
                flight.setArrDelay(parseFloat(csvRecord.get("ARR_DELAY")));

                // Status 
                flight.setCancelled(parseBoolean(csvRecord.get("CANCELLED")));
                flight.setCancellationCode(csvRecord.get("CANCELLATION_CODE"));
                flight.setDiverted(parseBoolean(csvRecord.get("DIVERTED")));

                // Duration & Distance
                flight.setCrsElapsedTime(parseFloat(csvRecord.get("CRS_ELAPSED_TIME")));
                flight.setElapsedTime(parseFloat(csvRecord.get("ELAPSED_TIME")));
                flight.setAirTime(parseFloat(csvRecord.get("AIR_TIME")));
                flight.setDistance(parseFloat(csvRecord.get("DISTANCE")));

                // Granular Delays
                flight.setDelayDueCarrier(parseFloat(csvRecord.get("DELAY_DUE_CARRIER")));
                flight.setDelayDueWeather(parseFloat(csvRecord.get("DELAY_DUE_WEATHER")));
                flight.setDelayDueNas(parseFloat(csvRecord.get("DELAY_DUE_NAS")));
                flight.setDelayDueSecurity(parseFloat(csvRecord.get("DELAY_DUE_SECURITY")));
                flight.setDelayDueLateAircraft(parseFloat(csvRecord.get("DELAY_DUE_LATE_AIRCRAFT")));

                flightsToSave.add(flight);
            }
            
            // Batch save flights
            flightRepository.saveAll(flightsToSave);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public List<Flight> getFlights(LocalDate date) {
        if (date != null) {
            return flightRepository.findByFlDate(date);
        }
        return flightRepository.findAll();
    }

    // Helper methods to safely handle empty CSV fields and float-formatted integers (like "1941.0")
    private Integer parseInteger(String val) {
        if (val == null || val.isEmpty()) return null;
        return (int) Double.parseDouble(val); 
    }

    private Short parseShort(String val) {
        if (val == null || val.isEmpty()) return null;
        return (short) Double.parseDouble(val);
    }

    private Float parseFloat(String val) {
        if (val == null || val.isEmpty()) return null;
        return Float.parseFloat(val);
    }

    private Boolean parseBoolean(String val) {
        if (val == null || val.isEmpty()) return false;
        return Double.parseDouble(val) == 1.0;
    }
}