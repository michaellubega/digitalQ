# digitalQ — Software Requirements Specification (SRS)

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |
| **Standard** | IEEE 830 |

---

## 1. Introduction

### 1.1 Purpose
This document defines the functional and non-functional requirements for digitalQ, a web-based queue management system with QR token verification.

### 1.2 Intended Audience
- Development team (implementation reference)
- Course instructor (evaluation criteria)
- Testers (test case derivation)

### 1.3 System Overview
digitalQ is a Spring Boot web application with three user roles:
- **Customer** — joins queue via browser, receives QR token, watches position
- **Counter Staff** — logs in, verifies QR tokens, marks customers as served
- **Administrator** — manages counters, monitors live dashboard, views reports

---

## 2. Functional Requirements

### FR-1: Customer Token Registration

| ID | Requirement | Priority |
|---|---|---|
| FR-1.1 | Customer enters name and selects a service type to join a queue | Must |
| FR-1.2 | System generates a unique token ID (e.g., A-001, B-002) | Must |
| FR-1.3 | System generates a QR code encoding the token ID | Must |
| FR-1.4 | Customer sees their token number, QR code, and current position | Must |
| FR-1.5 | Customer's position updates in real time as ahead tokens are served | Must |
| FR-1.6 | When customer's token is called, they see a "Your turn" notification | Must |
| FR-1.7 | Token expires if not served within a configurable timeout | Should |

### FR-2: Counter Staff Operations

| ID | Requirement | Priority |
|---|---|---|
| FR-2.1 | Counter staff logs in with username and password | Must |
| FR-2.2 | Staff sees their assigned counter and the current serving token | Must |
| FR-2.3 | Staff can scan or manually enter a QR token ID to verify it | Must |
| FR-2.4 | System validates token: must be valid, not expired, and in the correct queue | Must |
| FR-2.5 | Staff marks verified token as "served" — customer is removed from queue | Must |
| FR-2.6 | Staff can call the next token in queue | Must |
| FR-2.7 | Staff can see a list of waiting tokens for their counter | Must |
| FR-2.8 | Staff can pause/resume their counter | Should |

### FR-3: Administrator Dashboard

| ID | Requirement | Priority |
|---|---|---|
| FR-3.1 | Admin logs in with username and password | Must |
| FR-3.2 | Dashboard shows live stats: total waiting, total served, average wait time | Must |
| FR-3.3 | Admin can view all counters and their current status | Must |
| FR-3.4 | Admin can create new counters with a name and service type | Must |
| FR-3.5 | Admin can assign staff accounts to counters | Must |
| FR-3.6 | Admin can deactivate/reactivate counters | Must |
| FR-3.7 | Admin can view historical reports (tokens served per counter per day) | Should |
| FR-3.8 | Admin can manage staff accounts (create, edit, deactivate) | Must |

### FR-4: Authentication & Authorization

| ID | Requirement | Priority |
|---|---|---|
| FR-4.1 | Three roles: ADMIN, STAFF, CUSTOMER | Must |
| FR-4.2 | Admin and Staff require login; Customer is session-based (no login) | Must |
| FR-4.3 | Passwords stored as BCrypt hashes | Must |
| FR-4.4 | Role-based access control on all endpoints | Must |
| FR-4.5 | Session timeout after 30 minutes of inactivity | Should |

---

## 3. Non-Functional Requirements

| ID | Requirement | Target |
|---|---|---|
| NFR-1 | Token generation response time | < 500ms |
| NFR-2 | Real-time position update latency | < 1 second |
| NFR-3 | Concurrent users supported | 50+ |
| NFR-4 | Application startup time | < 10 seconds |
| NFR-5 | Browser compatibility | Chrome, Firefox, Safari, Edge (latest 2 versions) |
| NFR-6 | Data persistence | All tokens and audit logs stored in H2 file database |
| NFR-7 | Security | BCrypt password hashing, CSRF protection, session fixation protection |

---

## 4. User Stories

### US-1: Customer joins queue
> As a customer, I want to enter my name and pick a service so that I can get a token and wait remotely.

**Acceptance Criteria:**
- Name field is required (2-50 characters)
- Service type dropdown shows active counters
- After submission, token number and QR code are displayed
- Position updates automatically without refresh

### US-2: Counter staff serves customer
> As counter staff, I want to scan or type a token ID so that I can verify and serve the customer.

**Acceptance Criteria:**
- Token ID input field with "Verify" button
- Valid token shows customer name and "Serve" button
- Invalid/expired token shows error message
- Served token is removed from queue and next token is called

### US-3: Admin monitors operations
> As an administrator, I want a live dashboard so that I can see all counters and queue stats.

**Acceptance Criteria:**
- Dashboard auto-refreshes every 5 seconds
- Shows per-counter: waiting count, currently serving, total served today
- Shows system-wide: total waiting, total served, average wait time

---

## 5. Data Entities

| Entity | Key Fields |
|---|---|
| **User** | id, username, password (BCrypt), role, active |
| **Counter** | id, name, serviceType, active, currentToken |
| **Token** | id, tokenNumber, customerName, counterId, status, createdAt, servedAt, expiresAt |
| **StaffAssignment** | id, userId, counterId |

### Token Status Lifecycle
```
WAITING → CALLED → SERVED
                 ↓
              EXPIRED
```

---

## 6. Assumptions & Constraints

- Single-branch deployment (one physical location)
- H2 file database stored in `./data/digitalq`
- Default admin account: `admin` / `admin123` (changeable after first login)
- QR codes encode the token number as plain text (scannable by any QR reader)
- Application runs on port 8080
