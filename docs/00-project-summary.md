# digitalQ — Project Summary

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |
| **Status** | Complete |

---

## 1. Project Overview

digitalQ is a web-based queue management system built with Java and Spring Boot. It replaces physical waiting lines with a digital token system featuring QR code verification, real-time updates, and role-based access for administrators, counter staff, and customers.

## 2. SDLC Phases Completed

| Phase | Deliverable | Status |
|---|---|---|
| **Planning** | Project Charter (`docs/01-project-charter.md`) | Complete |
| **Requirements** | Software Requirements Specification (`docs/02-srs.md`) | Complete |
| **Design** | System Design Document (`docs/03-design-document.md`) | Complete |
| **Implementation** | Source code (Spring Boot application) | Complete |
| **Testing** | Test Plan (`docs/04-test-plan.md`) + 25 automated tests | Complete |
| **Deployment** | Deployment Guide (`docs/06-deployment-guide.md`) | Complete |
| **Documentation** | User Manual (`docs/05-user-manual.md`) | Complete |

## 3. Features Implemented

### Customer Features
- [x] Join queue via web browser
- [x] Receive unique QR-coded token
- [x] Real-time position tracking
- [x] Token status notifications (Waiting → Called → Served)

### Counter Staff Features
- [x] Secure login
- [x] QR token verification (scan or manual entry)
- [x] Serve customers
- [x] Call next token in queue
- [x] Pause/resume counter
- [x] View waiting queue

### Admin Features
- [x] Secure login
- [x] Live dashboard with real-time statistics
- [x] Counter management (create, edit, activate/deactivate)
- [x] Staff account management (create, edit, assign to counters)
- [x] Reports and analytics

## 4. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security (BCrypt, CSRF, Session Management) |
| Database | H2 (embedded, file-based) |
| ORM | Spring Data JPA |
| Real-time | Spring WebSocket (STOMP) |
| QR Code | ZXing |
| Frontend | Thymeleaf + Bootstrap 5 |
| Build | Maven |

## 5. Test Results

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Class | Tests | Status |
|---|---|---|
| TokenControllerTest | 7 | Pass |
| TokenServiceTest | 13 | Pass |
| QueueServiceTest | 5 | Pass |

## 6. How to Run

```bash
# Build
mvn clean package

# Run
java -jar target/digitalQ-1.0.0.jar

# Access
http://localhost:8080
```

### Default Accounts
| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Staff | `staff1` | `staff123` |

## 7. Project Structure

```
digitalQ/
├── docs/                          # SDLC documentation (6 documents)
├── src/
│   ├── main/
│   │   ├── java/com/digitalq/
│   │   │   ├── config/           # Security, WebSocket, Data Init
│   │   │   ├── controller/       # 4 controllers
│   │   │   ├── dto/              # 4 DTOs
│   │   │   ├── model/            # 4 entities
│   │   │   ├── repository/       # 4 repositories
│   │   │   └── service/          # 5 services
│   │   └── resources/
│   │       ├── static/           # CSS, JS
│   │       └── templates/        # 8 Thymeleaf templates
│   └── test/                     # 25 tests (3 test classes)
├── target/
│   └── digitalQ-1.0.0.jar        # Executable JAR (55MB)
├── pom.xml
├── mvnw / mvnw.cmd
├── .gitignore
└── README.md
```

## 8. API Endpoints

### Customer (Public)
- `GET /` — Landing page
- `POST /api/tokens` — Create token
- `GET /api/tokens/{tokenNumber}` — Token status
- `GET /token/{tokenNumber}` — Token page

### Staff (Authenticated)
- `GET /staff` — Dashboard
- `POST /staff/verify` — Verify token
- `POST /staff/serve/{tokenId}` — Serve token
- `POST /staff/next/{counterId}` — Call next
- `POST /staff/pause/{counterId}` — Pause counter
- `POST /staff/resume/{counterId}` — Resume counter

### Admin (Authenticated)
- `GET /admin` — Dashboard
- `GET /admin/counters` — Counter management
- `POST /admin/counters` — Create counter
- `GET /admin/staff` — Staff management
- `POST /admin/staff` — Create staff
- `GET /admin/reports` — Reports
- `GET /admin/api/stats` — Live stats (JSON)

## 9. Key Design Decisions

1. **H2 Embedded Database** — Zero setup, perfect for coursework demos
2. **WebSocket Real-time Updates** — No page refresh needed for queue updates
3. **QR Code Verification** — Contactless, secure token validation
4. **Role-based Security** — Spring Security with BCrypt password hashing
5. **Layered Architecture** — Clear separation: Controller → Service → Repository
6. **DTO Pattern** — Clean API contracts between layers

## 10. Future Enhancements

- [ ] Email/SMS notifications when token is called
- [ ] Multi-branch support
- [ ] Mobile native apps
- [ ] Payment integration
- [ ] Advanced analytics with charts
