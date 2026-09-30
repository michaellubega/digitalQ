# digitalQ — System Design Document

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |

---

## 1. Architecture Overview

digitalQ follows a layered architecture within a Spring Boot monolith:

```
┌─────────────────────────────────────────────────┐
│                   Browser (Client)               │
│  Customer Page  │  Staff Page  │  Admin Dashboard │
└────────┬────────┴──────┬───────┴────────┬────────┘
         │               │                │
    ┌────▼───────────────▼────────────────▼────┐
    │           Spring MVC Controllers          │
    │  CustomerController  StaffController      │
    │  AdminController    AuthController        │
    └────┬───────────────┬────────────────┬────┘
         │               │                │
    ┌────▼───────────────▼────────────────▼────┐
    │              Service Layer                │
    │  QueueService  TokenService               │
    │  CounterService  UserService             │
    └────┬───────────────┬────────────────┬────┘
         │               │                │
    ┌────▼───────────────▼────────────────▼────┐
    │           Repository Layer (JPA)          │
    │  TokenRepo  CounterRepo  UserRepo        │
    └────┬───────────────┬────────────────┬────┘
         │               │                │
    ┌────▼───────────────▼────────────────▼────┐
    │              H2 Database                  │
    └──────────────────────────────────────────┘
```

### Real-time Communication
- **WebSocket** with STOMP protocol for live queue updates
- Server pushes position changes to connected clients
- Topics: `/topic/queue/{counterId}`, `/topic/admin/stats`

---

## 2. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Framework | Spring Boot | 3.2.x |
| Security | Spring Security | 6.x |
| ORM | Spring Data JPA | 3.2.x |
| Database | H2 | 2.x |
| WebSocket | Spring WebSocket | 6.x |
| QR Code | ZXing | 3.5.x |
| Template Engine | Thymeleaf | 3.1.x |
| CSS Framework | Bootstrap | 5.3.x |
| Build | Maven | 3.9.x |

---

## 3. Database Design

### 3.1 Entity-Relationship Diagram

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│   User   │       │   Counter    │       │  Token   │
├──────────┤       ├──────────────┤       ├──────────┤
│ id (PK)  │──┐    │ id (PK)      │◄──┐   │ id (PK)  │
│ username │  │    │ name         │   │   │ tokenNum │
│ password │  │    │ serviceType  │   │   │ custName │
│ role     │  │    │ active       │   │   │ status   │
│ active   │  │    │ currentToken │   │   │ counterId│──┘
└──────────┘  │    └──────────────┘   │   │ createdAt│
              │                       │   │ servedAt │
              │    ┌──────────────┐   │   │ expiresAt│
              └───►│StaffAssignmt │   │   └──────────┘
                   ├──────────────┤   │
                   │ id (PK)      │   │
                   │ userId (FK)  │───┘
                   │ counterId(FK)│───┘
                   └──────────────┘
```

### 3.2 Table Definitions

#### `app_user`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR(50) | UNIQUE, NOT NULL |
| password | VARCHAR(100) | NOT NULL (BCrypt hash) |
| role | VARCHAR(20) | NOT NULL (ADMIN, STAFF) |
| active | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |

#### `counter`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| service_type | VARCHAR(100) | NOT NULL |
| active | BOOLEAN | DEFAULT TRUE |
| current_token | VARCHAR(20) | NULLABLE |

#### `token`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| token_number | VARCHAR(20) | UNIQUE, NOT NULL |
| customer_name | VARCHAR(100) | NOT NULL |
| counter_id | BIGINT | FK → counter.id |
| status | VARCHAR(20) | NOT NULL (WAITING, CALLED, SERVED, EXPIRED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP |
| served_at | TIMESTAMP | NULLABLE |
| expires_at | TIMESTAMP | NOT NULL |

#### `staff_assignment`
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| user_id | BIGINT | FK → app_user.id |
| counter_id | BIGINT | FK → counter.id |

### 3.3 Indexes
- `idx_token_counter_status` on `token(counter_id, status)`
- `idx_token_expires` on `token(expires_at)`
- `idx_token_number` on `token(token_number)` (unique)

---

## 4. API Design

### 4.1 Authentication Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/login` | Public | Authenticate user, create session |
| POST | `/logout` | Any | Invalidate session |
| GET | `/login` | Public | Login page |

### 4.2 Customer Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/` | Public | Customer landing page |
| POST | `/api/tokens` | Public | Create new token |
| GET | `/api/tokens/{tokenNumber}` | Public | Get token status |
| GET | `/token/{tokenNumber}` | Public | Token status page (with QR) |
| WS | `/topic/queue/{counterId}` | — | Real-time queue updates |

### 4.3 Staff Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/staff` | STAFF | Staff dashboard |
| POST | `/staff/verify` | STAFF | Verify a token by number |
| POST | `/staff/serve/{tokenId}` | STAFF | Mark token as served |
| POST | `/staff/next/{counterId}` | STAFF | Call next token |
| POST | `/staff/pause/{counterId}` | STAFF | Pause counter |
| POST | `/staff/resume/{counterId}` | STAFF | Resume counter |
| GET | `/staff/queue/{counterId}` | STAFF | Get waiting tokens |

### 4.4 Admin Endpoints

| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/admin` | ADMIN | Admin dashboard |
| GET | `/admin/counters` | ADMIN | Counter management page |
| POST | `/admin/counters` | ADMIN | Create counter |
| PUT | `/admin/counters/{id}` | ADMIN | Update counter |
| DELETE | `/admin/counters/{id}` | ADMIN | Deactivate counter |
| GET | `/admin/staff` | ADMIN | Staff management page |
| POST | `/admin/staff` | ADMIN | Create staff account |
| PUT | `/admin/staff/{id}` | ADMIN | Update staff account |
| GET | `/admin/reports` | ADMIN | Reports page |
| GET | `/admin/api/stats` | ADMIN | Live statistics (JSON) |

---

## 5. Security Design

### 5.1 Authentication
- Spring Security with form-based login
- BCrypt password encoder (strength 10)
- Session-based authentication (JSESSIONID cookie)

### 5.2 Authorization
- URL-based access control in `SecurityConfig`
- Role hierarchy: ADMIN > STAFF
- Public endpoints: `/`, `/login`, `/api/tokens`, `/token/**`, static resources

### 5.3 CSRF Protection
- CSRF tokens enabled for all state-changing requests
- Thymeleaf automatically includes CSRF tokens in forms

### 5.4 Session Management
- Session fixation protection (migrate session on login)
- Maximum sessions per user: 1
- Session timeout: 30 minutes

---

## 6. QR Code Design

- **Format**: Plain text token number (e.g., `A-001`)
- **Size**: 300x300 pixels
- **Error Correction**: Level M (15%)
- **Library**: ZXing (`com.google.zxing:core` and `javase`)
- **Delivery**: Base64-encoded PNG embedded in HTML `<img>` tag

---

## 7. Real-time Architecture

### WebSocket Flow
1. Customer subscribes to `/topic/queue/{counterId}` when viewing their token
2. Staff performs an action (verify, serve, call next)
3. Service layer publishes updated queue state to the topic
4. All subscribed clients receive the update and re-render

### Admin Dashboard Updates
- Admin dashboard polls `/admin/api/stats` every 5 seconds via AJAX
- Alternative: WebSocket subscription to `/topic/admin/stats`

---

## 8. Project Structure

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
│   │   │   ├── DigitalQApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebSocketConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── StaffController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Counter.java
│   │   │   │   ├── Token.java
│   │   │   │   └── StaffAssignment.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CounterRepository.java
│   │   │   │   └── TokenRepository.java
│   │   │   ├── service/
│   │   │   │   ├── QueueService.java
│   │   │   │   ├── TokenService.java
│   │   │   │   ├── CounterService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── QrCodeService.java
│   │   │   └── dto/
│   │   │       ├── TokenRequest.java
│   │   │       ├── TokenResponse.java
│   │   │       └── StatsResponse.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── static/
│   │       │   ├── css/
│   │       │   └── js/
│   │       └── templates/
│   │           ├── login.html
│   │           ├── customer/
│   │           │   ├── index.html
│   │           │   └── token.html
│   │           ├── staff/
│   │           │   └── dashboard.html
│   │           └── admin/
│   │               ├── dashboard.html
│   │               ├── counters.html
│   │               ├── staff.html
│   │               └── reports.html
│   └── test/
│       └── java/com/digitalq/
│           ├── service/
│           │   └── QueueServiceTest.java
│           └── controller/
│               └── TokenControllerTest.java
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```
