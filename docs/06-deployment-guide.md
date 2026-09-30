# digitalQ — Deployment Guide

| Field | Detail |
|---|---|
| **Project Name** | digitalQ |
| **Version** | 1.0 |
| **Date** | 2026-10-01 |

---

## 1. Prerequisites

| Requirement | Version | Download |
|---|---|---|
| Java JDK | 17+ | https://adoptium.net/ |
| Maven | 3.9+ | https://maven.apache.org/ |
| Web Browser | Latest | Chrome, Firefox, Safari, Edge |

> **Note:** Maven is optional if you use the included Maven wrapper (`mvnw`).

---

## 2. Building the Application

### 2.1 Using Maven Wrapper (Recommended)

**macOS / Linux:**
```bash
cd digitalQ
chmod +x mvnw
./mvnw clean package
```

**Windows:**
```cmd
cd digitalQ
mvnw.cmd clean package
```

### 2.2 Using System Maven
```bash
cd digitalQ
mvn clean package
```

### 2.3 Build Output
- JAR file: `target/digitalQ-1.0.0.jar`
- Size: ~50-80 MB (includes embedded dependencies)

---

## 3. Running the Application

### 3.1 Run with Maven
```bash
./mvnw spring-boot:run
```

### 3.2 Run the JAR
```bash
java -jar target/digitalQ-1.0.0.jar
```

### 3.3 Run in Background (Production)
```bash
nohup java -jar target/digitalQ-1.0.0.jar > digitalq.log 2>&1 &
```

### 3.4 Access the Application
- URL: `http://localhost:8080`
- Default admin: `admin` / `admin123`
- Default staff: `staff1` / `staff123`

---

## 4. Configuration

### 4.1 Application Properties
File: `src/main/resources/application.properties`

| Property | Default | Description |
|---|---|---|
| `server.port` | 8080 | HTTP port |
| `spring.datasource.url` | jdbc:h2:file:./data/digitalq | H2 database file path |
| `spring.jpa.hibernate.ddl-auto` | update | Auto-create/update tables |
| `spring.h2.console.enabled` | true | Enable H2 web console |
| `digitalq.token.expiry-minutes` | 30 | Token expiration time |
| `digitalq.admin.username` | admin | Default admin username |
| `digitalq.admin.password` | admin123 | Default admin password |

### 4.2 Changing the Port
```properties
server.port=9090
```

### 4.3 Changing Token Expiry
```properties
digitalq.token.expiry-minutes=15
```

### 4.4 Accessing H2 Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/digitalq`
- Username: `sa`
- Password: (leave empty)

---

## 5. Database Management

### 5.1 Database Location
- H2 file database: `./data/digitalq.mv.db`
- Created automatically on first run

### 5.2 Backup
```bash
cp -r data/ data-backup-$(date +%Y%m%d)/
```

### 5.3 Reset Database
```bash
# Stop the application first
rm -rf data/
# Restart — tables will be recreated with default data
```

---

## 6. Production Deployment

### 6.1 System Requirements
| Resource | Minimum | Recommended |
|---|---|---|
| RAM | 512 MB | 2 GB |
| Disk | 100 MB | 1 GB |
| CPU | 1 core | 2 cores |

### 6.2 Running as a Service (Linux)

Create `/etc/systemd/system/digitalq.service`:
```ini
[Unit]
Description=digitalQ Queue Management System
After=network.target

[Service]
User=digitalq
WorkingDirectory=/opt/digitalQ
ExecStart=/usr/bin/java -jar /opt/digitalQ/digitalQ-1.0.0.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable digitalq
sudo systemctl start digitalq
sudo systemctl status digitalq
```

### 6.3 Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/digitalQ-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t digitalq .
docker run -p 8080:8080 -v digitalq-data:/app/data digitalq
```

---

## 7. Security Checklist for Production

- [ ] Change default admin password
- [ ] Change default staff passwords
- [ ] Disable H2 console (`spring.h2.console.enabled=false`)
- [ ] Enable HTTPS with SSL certificate
- [ ] Configure firewall (allow only port 8080/443)
- [ ] Set up log rotation
- [ ] Configure database backups
- [ ] Remove or secure `/actuator` endpoints

---

## 8. Stopping the Application

### Interactive Mode
Press `Ctrl + C` in the terminal

### Background Process
```bash
# Find the process
ps aux | grep digitalQ
# Kill it
kill <PID>
```

### Systemd Service
```bash
sudo systemctl stop digitalq
```
