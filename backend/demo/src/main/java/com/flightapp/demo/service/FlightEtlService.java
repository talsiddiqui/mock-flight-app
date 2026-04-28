package com.flightapp.demo.service;

import com.flightapp.demo.model.Airline;
import com.flightapp.demo.model.Airport;
import com.flightapp.demo.model.Flight;
import com.flightapp.demo.repository.AirlineRepository;
import com.flightapp.demo.repository.AirportRepository;
import com.flightapp.demo.repository.FlightRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlightEtlService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final AirportRepository airportRepository;
    
    // Thread-safe in-memory cache for processed file hashes
    private final Set<String> processedHashes = ConcurrentHashMap.newKeySet();
    private final Path scheduleDir = Paths.get("data", "schedule");

    public FlightEtlService(FlightRepository flightRepository, AirlineRepository airlineRepository, AirportRepository airportRepository) {
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
        this.airportRepository = airportRepository;
    }

    // Ensures the polling directory exists on startup
    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(scheduleDir)) {
                Files.createDirectories(scheduleDir);
                System.out.println("Created ingestion directory at: " + scheduleDir.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Could not create schedule directory: " + e.getMessage());
        }
    }

    // Runs every 5 minutes (300,000 milliseconds)
    @Scheduled(fixedDelay = 300000)
    public void pollDirectoryForNewFiles() {
        if (!Files.exists(scheduleDir)) return;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(scheduleDir, "*.csv")) {
            for (Path file : stream) {
                String fileHash = calculateHash(file);
                
                // .add() returns true if the hash wasn't already in the Set
                if (processedHashes.add(fileHash)) {
                    System.out.println("New file detected: " + file.getFileName());
                    
                    try (InputStream is = Files.newInputStream(file)) {
                        processCsvStream(is);
                        System.out.println("Successfully ingested: " + file.getFileName());
                    } catch (Exception e) {
                        System.err.println("Failed to process " + file.getFileName() + ": " + e.getMessage());
                        // Remove hash so it tries again on the next cycle
                        processedHashes.remove(fileHash); 
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }

    // Generates SHA-256 hash in a memory-safe way for large files
    private String calculateHash(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(file);
             DigestInputStream dis = new DigestInputStream(is, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // Consume the stream to update the digest
            }
        }
        
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // REST controller entry point
    public void processCsv(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            processCsvStream(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process uploaded file", e);
        }
    }

    public List<Flight> getFlights(LocalDate date) {
        if (date != null) {
            return flightRepository.findByFlDate(date);
        }
        return flightRepository.findAll();
    }

    // Core ETL Logic - refactored to accept standard InputStream
    @Transactional
    public void processCsvStream(InputStream inputStream) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<Flight> flightsToSave = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                // 1. Process Airline
                Airline airline = new Airline();
                airline.setAirlineCode(csvRecord.get("AIRLINE_CODE"));
                airline.setAirline(csvRecord.get("AIRLINE"));
                airline.setAirlineDot(csvRecord.get("AIRLINE_DOT"));
                airline.setDotCode(parseInteger(csvRecord.get("DOT_CODE")));
                airlineRepository.save(airline);

                // 2. Process Airports
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

                flight.setCancelled(parseBoolean(csvRecord.get("CANCELLED")));
                flight.setCancellationCode(csvRecord.get("CANCELLATION_CODE"));
                flight.setDiverted(parseBoolean(csvRecord.get("DIVERTED")));

                flight.setCrsElapsedTime(parseFloat(csvRecord.get("CRS_ELAPSED_TIME")));
                flight.setElapsedTime(parseFloat(csvRecord.get("ELAPSED_TIME")));
                flight.setAirTime(parseFloat(csvRecord.get("AIR_TIME")));
                flight.setDistance(parseFloat(csvRecord.get("DISTANCE")));

                flight.setDelayDueCarrier(parseFloat(csvRecord.get("DELAY_DUE_CARRIER")));
                flight.setDelayDueWeather(parseFloat(csvRecord.get("DELAY_DUE_WEATHER")));
                flight.setDelayDueNas(parseFloat(csvRecord.get("DELAY_DUE_NAS")));
                flight.setDelayDueSecurity(parseFloat(csvRecord.get("DELAY_DUE_SECURITY")));
                flight.setDelayDueLateAircraft(parseFloat(csvRecord.get("DELAY_DUE_LATE_AIRCRAFT")));

                flightsToSave.add(flight);
            }
            
            flightRepository.saveAll(flightsToSave);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV stream: " + e.getMessage());
        }
    }

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