# ✈️ Flight Data ETL Project

A full-stack data engineering project that ingests flight CSV data into a PostgreSQL database using a Spring Boot ETL pipeline and displays results via a Next.js UI. The entire system is containerized using Docker.

---

## 🧱 Tech Stack

- Backend: Spring Boot (Java)
- Database: PostgreSQL
- Frontend: Next.js (React)
- ETL: CSV ingestion via Spring Boot
- Containerization: Docker + Docker Compose

---

## 📊 Architecture Overview

```

CSV File Upload
↓
Spring Boot ETL API
↓
PostgreSQL Database
↓
Next.js Frontend UI

```

---

## 📁 Project Structure

```

flight-app/
│
├── backend/        # Spring Boot ETL + API
├── frontend/       # Next.js UI
├── database/       # SQL init scripts
├── docker-compose.yml
└── README.md

````

---

## 🚀 Getting Started

### 1. Prerequisites

- Docker Desktop installed
- Java 17 (for local backend dev)
- Node.js 18+ (for frontend dev)
- Maven (if running backend outside Docker)

---

## 🐳 Running with Docker (Recommended)

From the project root:

```bash
docker compose up --build
````

Services will start:

| Service  | URL                                            |
| -------- | ---------------------------------------------- |
| Frontend | [http://localhost:3000](http://localhost:3000) |
| Backend  | [http://localhost:8080](http://localhost:8080) |
| Postgres | localhost:5432                                 |

---

## 📤 CSV Upload Flow

1. Open frontend UI
2. Upload flight CSV file
3. File is sent to:

```
POST /api/upload
```

4. Spring Boot:

   * Parses CSV
   * Normalizes airlines + airports
   * Inserts flights into PostgreSQL

---

## 🗄️ Database Schema

* airlines (carrier metadata)
* airports (airport + city mapping)
* flights (main fact table with delay metrics)

---

## 🔌 Backend API

### Upload CSV

```
POST /api/upload
```

**Request:**

* multipart file (`file`)

**Response:**

* success / error message

---

### Future endpoints (planned)

* GET /api/flights
* GET /api/flights?date=YYYY-MM-DD
* GET /api/airlines
* GET /api/airports

---

## 🧪 Running Locally (without Docker)

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## ⚙️ Environment Variables

Backend (`application.yml`):

* DB URL
* DB username
* DB password

Example (Docker):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/flightdb
    username: flightuser
    password: flightpass
```

---

## 🧠 Key Features

* CSV-based ETL pipeline
* Airline and airport normalization
* Relational schema design
* Dockerized full stack setup
* Modular Spring Boot architecture

---

## 📌 Notes

* Designed for learning Spring Boot + ETL concepts
* Not optimized for production scale ingestion
* Focus is correctness + clarity over performance

---

## 📈 Future Improvements

* Batch inserts for ETL performance
* Pagination for flight queries
* Filtering by airline/airport/date
* Charts in frontend (delays, routes)
* Authentication layer
* Async ingestion queue
