# digitalQ — User Manual

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |

---

## 1. Getting Started

### 1.1 Accessing the System
1. Open your web browser (Chrome, Firefox, Safari, or Edge)
2. Navigate to: `http://localhost:8080`
3. You will see the customer landing page

### 1.2 Default Accounts

| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |
| Counter Staff | `staff1` | `staff123` |

> **Important:** Change the default admin password after first login for security.

---

## 2. Customer Guide

### 2.1 Joining a Queue

1. On the landing page, enter your **name** (2-50 characters)
2. Select a **service type** from the dropdown (e.g., "Billing", "Support")
3. Click **"Get Token"**
4. Your token will be displayed with:
   - Token number (e.g., `A-001`)
   - QR code
   - Current position in queue
   - Estimated wait time

### 2.2 Viewing Your Token

- Navigate to `http://localhost:8080/token/{tokenNumber}` (e.g., `/token/A-001`)
- Your token status page shows:
  - Token number and QR code
  - Current position (updates in real time)
  - Status: Waiting → Called → Served

### 2.3 Understanding Token Statuses

| Status | Meaning |
|---|---|
| **WAITING** | You are in the queue |
| **CALLED** | Your token has been called — proceed to the counter |
| **SERVED** | You have been served |
| **EXPIRED** | Your token timed out (did not respond in time) |

---

## 3. Counter Staff Guide

### 3.1 Logging In

1. Navigate to `http://localhost:8080/login`
2. Enter your staff username and password
3. Click **"Login"**
4. You will be redirected to the staff dashboard

### 3.2 Staff Dashboard

The dashboard shows:
- Your assigned counter name
- Currently serving token (if any)
- List of waiting tokens
- Queue statistics

### 3.3 Verifying a Token

1. When a customer arrives, they show you their QR code
2. **Scan** the QR code with a QR reader, or **type** the token number manually
3. Click **"Verify"**
4. The system will validate the token:
   - ✅ Valid: Customer name is displayed, click **"Serve"** to complete
   - ❌ Invalid: Error message (expired, already served, or not found)

### 3.4 Calling the Next Token

1. Click **"Call Next"** on your dashboard
2. The next waiting token is marked as **CALLED**
3. The customer sees a "Your turn" notification on their screen
4. The counter's `currentToken` is updated

### 3.5 Pausing/Resuming Your Counter

- Click **"Pause"** to temporarily stop accepting new customers
- Click **"Resume"** to reactivate your counter
- Paused counters are hidden from the customer service dropdown

---

## 4. Administrator Guide

### 4.1 Logging In

1. Navigate to `http://localhost:8080/login`
2. Enter admin username and password
3. Click **"Login"**
4. You will be redirected to the admin dashboard

### 4.2 Admin Dashboard

The dashboard auto-refreshes every 5 seconds and shows:

| Statistic | Description |
|---|---|
| Total Waiting | Number of tokens currently in all queues |
| Total Served Today | Tokens marked as SERVED today |
| Average Wait Time | Average time from token creation to service |
| Active Counters | Number of currently active counters |

### 4.3 Managing Counters

1. Go to **"Counters"** in the navigation
2. **Create Counter**: Click "New Counter", enter name and service type
3. **Edit Counter**: Click "Edit" on any counter row
4. **Deactivate Counter**: Click "Deactivate" — counter becomes inactive and hidden from customers
5. **Reactivate Counter**: Click "Activate" on an inactive counter

### 4.4 Managing Staff Accounts

1. Go to **"Staff"** in the navigation
2. **Create Staff**: Click "New Staff", enter username, password, and assign to a counter
3. **Edit Staff**: Click "Edit" to change password or counter assignment
4. **Deactivate Staff**: Click "Deactivate" — staff member can no longer log in

### 4.5 Viewing Reports

1. Go to **"Reports"** in the navigation
2. Select a date range
3. View:
   - Tokens served per counter per day
   - Average wait time per counter
   - Peak hours analysis

---

## 5. Troubleshooting

| Problem | Solution |
|---------|----------|
| "Token not found" error | Check the token number is correct (format: X-999) |
| "Token expired" error | Customer needs to get a new token |
| Cannot log in | Verify username/password; check if account is active |
| Dashboard not refreshing | Check browser console for errors; try hard refresh (Ctrl+F5) |
| QR code not scanning | Ensure adequate lighting; try manual token entry |
| Port 8080 already in use | Stop the other application or change port in `application.properties` |

---

## 6. Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + R` | Refresh current page |
| `Esc` | Close modal dialogs |
| `Enter` | Submit forms |
