# digitalQ — Queue Management System

A web-based queue management system with QR token verification, built with Spring Boot.

## Features

- **Customer Portal** — Join queue via browser, receive QR token, track position in real time
- **Counter Staff Dashboard** — Verify QR tokens, serve customers, manage queue
- **Admin Dashboard** — Live statistics, counter management, staff management, reports
- **Real-time Updates** — WebSocket-powered live queue position updates
- **QR Code System** — Each token has a unique QR code for secure verification
- **Role-based Access** — Three roles: Admin, Counter Staff, Customer

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security |
| Database | H2 (embedded) |
| ORM | Spring Data JPA |
| Real-time | Spring WebSocket |
| QR Code | ZXing |
| Frontend | Thymeleaf + Bootstrap 5 |
| Build | Maven |

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+ (or use the included Maven wrapper)

### Run the Application

**Using Maven Wrapper (macOS/Linux):**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

**Using Maven Wrapper (Windows):**
```cmd
mvnw.cmd spring-boot:run
```

**Using System Maven:**
```bash
mvn spring-boot:run
```

### Access the Application
- URL: `http://localhost:8080`
- Default admin: `admin` / `admin123`
- Default staff: `staff1` / `staff123`

## Project Structure

```
digitalQ/
├── docs/                          # SDLC documentation
│   ├── 01-project-charter.md
│   ├── 02-srs.md
│   ├── 03-design-document.md
│   ├── 04-test-plan.md
│   ├── 05-user-manual.md
│   └── 06-deployment-guide.md
├── src/
│   ├── main/
│   │   ├── java/com/digitalq/
│   │   │   ├── config/           # Security, WebSocket, Data Init
│   │   │   ├── controller/       # REST & MVC controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── model/            # JPA Entities
│   │   │   ├── repository/       # Spring Data Repositories
│   │   │   └── service/          # Business Logic
│   │   └── resources/
│   │       ├── static/           # CSS, JS
│   │       └── templates/        # Thymeleaf HTML
│   └── test/                     # Unit & Integration Tests
├── data/                         # H2 Database files (auto-created)
├── pom.xml
├── mvnw / mvnw.cmd
└── README.md
```

## Documentation

All SDLC documents are in the [`docs/`](docs/) folder:

1. [Project Charter](docs/01-project-charter.md) — Vision, scope, objectives
2. [SRS](docs/02-srs.md) — Functional & non-functional requirements
3. [Design Document](docs/03-design-document.md) — Architecture, DB schema, API design
4. [Test Plan](docs/04-test-plan.md) — Test strategy & test cases
5. [User Manual](docs/05-user-manual.md) — How to use the system
6. [Deployment Guide](docs/06-deployment-guide.md) — Build, run, deploy

## API Endpoints

### Customer
- `GET /` — Customer landing page
- `POST /api/tokens` — Create new token
- `GET /api/tokens/{tokenNumber}` — Get token status
- `GET /token/{tokenNumber}` — Token status page

### Staff
- `GET /staff` — Staff dashboard
- `POST /staff/verify` — Verify token
- `POST /staff/serve/{tokenId}` — Mark token as served
- `POST /staff/next/{counterId}` — Call next token
- `POST /staff/pause/{counterId}` — Pause counter
- `POST /staff/resume/{counterId}` — Resume counter

### Admin
- `GET /admin` — Admin dashboard
- `GET /admin/counters` — Counter management
- `POST /admin/counters` — Create counter
- `GET /admin/staff` — Staff management
- `POST /admin/staff` — Create staff
- `GET /admin/reports` — Reports
- `GET /admin/api/stats` — Live statistics (JSON)

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn verify
```

## License

This project is created for educational purposes.
