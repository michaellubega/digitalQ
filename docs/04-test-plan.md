# digitalQ — Test Plan

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |

---

## 1. Test Strategy

### 1.1 Testing Levels

| Level | Scope | Tool |
|---|---|---|
| Unit Testing | Service layer methods | JUnit 5, Mockito |
| Integration Testing | Controller endpoints, database | Spring Boot Test, H2 |
| System Testing | End-to-end workflows | Manual + Selenium (optional) |
| Security Testing | Authentication, authorization | Manual + Spring Security Test |

### 1.2 Test Environment
- Java 17
- Maven 3.9+
- H2 in-memory database (for tests)
- Port 8080

---

## 2. Test Cases

### 2.1 Authentication Tests

| ID | Test Case | Input | Expected Result |
|---|---|---|---|
| AUTH-01 | Admin login with valid credentials | username=admin, password=admin123 | Redirect to /admin |
| AUTH-02 | Admin login with invalid password | username=admin, password=wrong | Error message on login page |
| AUTH-03 | Staff login with valid credentials | username=staff1, password=staff123 | Redirect to /staff |
| AUTH-04 | Staff login with invalid credentials | username=staff1, password=wrong | Error message on login page |
| AUTH-05 | Unauthenticated access to /admin | No session | Redirect to /login |
| AUTH-06 | Unauthenticated access to /staff | No session | Redirect to /login |
| AUTH-07 | Staff accessing /admin | Staff session | 403 Forbidden |
| AUTH-08 | Admin accessing /staff | Admin session | Redirect to /admin (admin has access) |
| AUTH-09 | Logout | Click logout | Session invalidated, redirect to /login |
| AUTH-10 | Session timeout | Wait 30 min | Redirect to /login |

### 2.2 Customer Token Tests

| ID | Test Case | Input | Expected Result |
|---|---|---|---|
| CUST-01 | Create token with valid data | name="John", counterId=1 | Token created, QR displayed |
| CUST-02 | Create token with empty name | name="" | Validation error |
| CUST-03 | Create token with short name | name="A" | Validation error (min 2 chars) |
| CUST-04 | Create token with long name | name="A"*51 | Validation error (max 50 chars) |
| CUST-05 | Token number uniqueness | Create 100 tokens | All token numbers unique |
| CUST-06 | Token status page | GET /token/A-001 | Shows token details and QR |
| CUST-07 | Token position accuracy | 5 tokens in queue | Positions 1-5 correct |
| CUST-08 | Token expires after timeout | Wait past expiresAt | Status changes to EXPIRED |

### 2.3 Staff Operations Tests

| ID | Test Case | Input | Expected Result |
|---|---|---|---|
| STAFF-01 | Verify valid waiting token | tokenNumber=A-001 | Token details returned |
| STAFF-02 | Verify invalid token | tokenNumber=ZZZ-999 | Error: token not found |
| STAFF-03 | Verify expired token | tokenNumber=expired | Error: token expired |
| STAFF-04 | Verify already-served token | tokenNumber=served | Error: already served |
| STAFF-05 | Serve verified token | tokenId=1 | Status → SERVED, counter advances |
| STAFF-06 | Call next token | counterId=1 | Next WAITING token → CALLED |
| STAFF-07 | Pause counter | counterId=1 | Counter status → PAUSED |
| STAFF-08 | Resume counter | counterId=1 | Counter status → ACTIVE |
| STAFF-09 | Staff sees waiting list | counterId=1 | List of WAITING tokens returned |

### 2.4 Admin Dashboard Tests

| ID | Test Case | Input | Expected Result |
|---|---|---|---|
| ADMIN-01 | Dashboard loads | GET /admin | Shows stats and counters |
| ADMIN-02 | Create counter | name="Counter 4", serviceType="Billing" | Counter created |
| ADMIN-03 | Create counter with duplicate name | name="Counter 1" | Error: name exists |
| ADMIN-04 | Deactivate counter | counterId=1 | Counter active=false |
| ADMIN-05 | Create staff account | username="staff2", password="pass" | Staff created |
| ADMIN-06 | Assign staff to counter | userId=2, counterId=1 | Assignment created |
| ADMIN-07 | View reports | GET /admin/reports | Historical data displayed |
| ADMIN-08 | Live stats accuracy | 3 waiting, 2 served | Stats show 3 and 2 |

### 2.5 QR Code Tests

| ID | Test Case | Input | Expected Result |
|---|---|---|---|
| QR-01 | QR code generation | tokenNumber=A-001 | Valid PNG image generated |
| QR-02 | QR code content | Scan QR | Decodes to "A-001" |
| QR-03 | QR code uniqueness | 100 tokens | All QR codes unique |

---

## 3. Test Execution Plan

### Phase 1: Unit Tests (Automated)
```bash
mvn test
```
- Run all unit tests
- Target: 80%+ code coverage on service layer

### Phase 2: Integration Tests (Automated)
```bash
mvn verify
```
- Run Spring Boot integration tests
- Test controller endpoints with MockMvc

### Phase 3: System Tests (Manual)
1. Start application: `mvn spring-boot:run`
2. Execute all manual test cases above
3. Record results in test log

### Phase 4: Security Tests (Manual)
1. Attempt unauthorized access to all protected endpoints
2. Verify CSRF protection on forms
3. Test session management

---

## 4. Defect Severity Levels

| Level | Description | Example |
|---|---|---|
| Critical | System crash or data loss | Database corruption on token creation |
| Major | Feature broken | Token verification fails |
| Minor | UI or cosmetic issue | QR code alignment slightly off |
| Trivial | Enhancement | Add dark mode to dashboard |

---

## 5. Test Deliverables
- Test plan (this document)
- Test case execution log
- Defect report
- Test summary report
