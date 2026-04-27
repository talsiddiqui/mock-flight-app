-- 1. Table for Airlines
-- Stores unique carrier information to avoid repeating long strings in every flight row.
CREATE TABLE airlines (
    airline_code VARCHAR(10) PRIMARY KEY, -- Unique Carrier Code (e.g., UA, PA(1))
    airline TEXT,                         -- Full Airline Name (e.g., United Air Lines Inc.)
    airline_dot TEXT,                     -- DOT reporting name
    dot_code INTEGER                      -- Unique US DOT identification number
);

-- 2. Table for Airports
-- Stores airport codes and their associated city locations.
CREATE TABLE airports (
    airport_code CHAR(3) PRIMARY KEY,     -- 3-letter IATA code (e.g., FLL, EWR)
    city TEXT NOT NULL                    -- City and State name (e.g., "Fort Lauderdale, FL")
);

-- 3. Table for Flights
-- The main fact table containing flight-specific metrics and foreign keys.
CREATE TABLE flights (
    flight_id SERIAL PRIMARY KEY,         -- Internal surrogate key for specific flight records
    fl_date DATE NOT NULL,                -- Flight Date
    airline_code VARCHAR(10) REFERENCES airlines(airline_code), -- Reference to airlines table
    fl_number INTEGER,                    -- Flight Number
    
    -- Route References
    origin CHAR(3) REFERENCES airports(airport_code), -- Origin Airport Code
    dest CHAR(3) REFERENCES airports(airport_code),   -- Destination Airport Code

    -- Departure Performance
    crs_dep_time SMALLINT,                -- Scheduled departure (HHMM)
    dep_time SMALLINT,                    -- Actual departure (HHMM)
    dep_delay REAL,                       -- Departure delay in minutes
    taxi_out REAL,                        -- Taxi out time in minutes
    wheels_off SMALLINT,                  -- Wheels off time (HHMM)

    -- Arrival Performance
    wheels_on SMALLINT,                   -- Wheels on time (HHMM)
    taxi_in REAL,                         -- Taxi in time in minutes
    crs_arr_time SMALLINT,                -- Scheduled arrival (HHMM)
    arr_time SMALLINT,                    -- Actual arrival (HHMM)
    arr_delay REAL,                       -- Arrival delay in minutes

    -- Status and Cancellations
    cancelled BOOLEAN DEFAULT FALSE,      -- Converted from 1.0/0.0 float
    cancellation_code CHAR(1),            -- Reason for cancellation
    diverted BOOLEAN DEFAULT FALSE,       -- Converted from 1.0/0.0 float

    -- Time and Distance
    crs_elapsed_time REAL,                -- Scheduled flight duration
    elapsed_time REAL,                    -- Actual flight duration
    air_time REAL,                        -- Flight time in minutes
    distance REAL,                        -- Distance in miles

    -- Delay Breakdown (Minutes)
    delay_due_carrier REAL,               -- Carrier delay
    delay_due_weather REAL,               -- Weather delay
    delay_due_nas REAL,                   -- NAS delay
    delay_due_security REAL,              -- Security delay
    delay_due_late_aircraft REAL          -- Late aircraft delay
);

-- Speed up joins between flights and airlines/airports
CREATE INDEX idx_flights_airline ON flights (airline_code);
CREATE INDEX idx_flights_origin ON flights (origin);
CREATE INDEX idx_flights_dest ON flights (dest);

-- Optimize date-based analysis
CREATE INDEX idx_flights_date ON flights (fl_date);