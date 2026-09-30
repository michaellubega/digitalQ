# digitalQ — Project Charter

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |
| **Status** | Approved |
| **Stakeholders** | Course Instructor, Development Team |

---

## 1. Vision

digitalQ is a web-based queue management system that replaces physical waiting lines with a digital token system. Customers join a queue from their browser, receive a QR-coded token, and watch their position update in real time. Counter staff verify and serve tokens, while administrators monitor operations through a live dashboard.

## 2. Problem Statement

Traditional queue management (take a number, wait, guess when it's your turn) is inefficient, frustrating, and opaque. digitalQ solves this by:

- Letting customers join remotely and see live position updates
- Using QR tokens for secure, contactless verification
- Giving staff simple tools to call and verify the next customer
- Giving administrators real-time visibility and control

## 3. Goals & Objectives

| # | Goal | Measurable Objective |
|---|---|---|
| G1 | Digital token issuance | Customer receives a unique QR token within 2 seconds of joining |
| G2 | Real-time queue updates | Position changes reflect on all screens within 1 second |
| G3 | Secure verification | Only valid, unexpired tokens can be served |
| G4 | Administrative oversight | Admin dashboard shows live stats for all counters |
| G5 | Role-based access | Three distinct roles: Admin, Counter Staff, Customer |

## 4. Scope

### In Scope
- Customer self-service token registration (web browser)
- QR code generation and display for each token
- Counter staff login and token verification workflow
- Admin login, dashboard, counter management, and reports
- Real-time queue position updates via WebSocket
- H2 embedded database (file-based, zero external setup)

### Out of Scope
- Mobile native apps (iOS/Android)
- Payment processing
- SMS/email notifications
- Multi-branch support

## 5. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security |
| Database | H2 (embedded, file-based) |
| ORM | Spring Data JPA |
| Real-time | Spring WebSocket (STOMP) |
| QR Code | ZXing (Zebra Crossing) |
| Frontend | Thymeleaf + Bootstrap 5 |
| Build Tool | Maven |

## 6. Project Deliverables

1. SDLC documentation (this folder)
2. Source code (Spring Boot application)
3. Automated tests (unit + integration)
4. User manual
5. Deployment guide

## 7. Success Criteria

- All three user roles can complete their core workflows without errors
- QR tokens are unique, verifiable, and expire correctly
- Dashboard updates in real time without page refresh
- Application builds and runs with a single `mvn spring-boot:run` command
