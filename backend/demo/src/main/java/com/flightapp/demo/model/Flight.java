package com.flightapp.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "flights")
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id")
    private Long flightId;

    @Column(name = "fl_date", nullable = false)
    private LocalDate flDate;

    @ManyToOne
    @JoinColumn(name = "airline_code", referencedColumnName = "airline_code")
    private Airline airline;

    @Column(name = "fl_number")
    private Integer flNumber;

    @ManyToOne
    @JoinColumn(name = "origin", referencedColumnName = "airport_code")
    private Airport origin;

    @ManyToOne
    @JoinColumn(name = "dest", referencedColumnName = "airport_code")
    private Airport dest;

    @Column(name = "crs_dep_time") private Short crsDepTime;
    @Column(name = "dep_time") private Short depTime;
    @Column(name = "dep_delay") private Float depDelay;
    @Column(name = "taxi_out") private Float taxiOut;
    @Column(name = "wheels_off") private Short wheelsOff;
    @Column(name = "wheels_on") private Short wheelsOn;
    @Column(name = "taxi_in") private Float taxiIn;
    @Column(name = "crs_arr_time") private Short crsArrTime;
    @Column(name = "arr_time") private Short arrTime;
    @Column(name = "arr_delay") private Float arrDelay;
    @Column(name = "cancelled") private Boolean cancelled;
    @Column(name = "cancellation_code", length = 1) private String cancellationCode;
    @Column(name = "diverted") private Boolean diverted;
    @Column(name = "crs_elapsed_time") private Float crsElapsedTime;
    @Column(name = "elapsed_time") private Float elapsedTime;
    @Column(name = "air_time") private Float airTime;
    @Column(name = "distance") private Float distance;
    @Column(name = "delay_due_carrier") private Float delayDueCarrier;
    @Column(name = "delay_due_weather") private Float delayDueWeather;
    @Column(name = "delay_due_nas") private Float delayDueNas;
    @Column(name = "delay_due_security") private Float delayDueSecurity;
    @Column(name = "delay_due_late_aircraft") private Float delayDueLateAircraft;
}