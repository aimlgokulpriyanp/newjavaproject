# GrievanceTrack — Citizen Complaint Tracking with SLA Escalation

A production-grade, full-stack municipal civic grievance redressal and tracking system built with **Spring Boot 3**, **Spring Data JPA**, **MySQL / H2 (MySQL Mode)**, and a **Civic UI/UX Design System**.

---

## 🏛️ Project Overview

**GrievanceTrack** empowers citizens to register civic complaints (roads, water supply, sanitation, electricity, etc.), automatically routes them to responsible municipal departments, monitors resolution turnaround against category-defined **Service Level Agreements (SLAs)**, automatically escalates breached complaints to senior municipal officers every minute, and collects citizen feedback ratings post-resolution.

---

## 🚀 Key Features & Business Rules Implemented

| Category | Feature | Description |
| :--- | :--- | :--- |
| **Grievance Filing** | Auto-Generated Code | Generates unique sequential codes (`GRV-000001`, `GRV-000002`, ...) |
| | Category SLA Calculation | Deadline automatically calculated: `slaDeadline = createdAt + category.slaDays` |
| | Automatic Department Routing | Complaint assigned to category's responsible municipal department upon creation |
| **State Machine** | Strict Lifecycle Transitions | `PENDING` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` / `ESCALATED` / `REJECTED` |
| | Validation Guard | Invalid jumps (e.g., `PENDING` directly to `RESOLVED`) return `400 Bad Request` |
| **SLA & Escalation** | Automated Cron Engine | `@Scheduled(cron = "0 * * * * *")` audits overdue complaints every 60 seconds |
| | Escalation Audit Logging | Overdue complaints marked `ESCALATED`, assigned to Senior Officer with timestamp & reason |
| | Duplicate Prevention | Escalation engine prevents duplicate escalation records for already escalated complaints |
| **Citizen Feedback** | 1–5 Star Rating | Allowed strictly after resolution; duplicate ratings rejected with `409 Conflict` |
| **Notifications** | Civic Alert Log | System logs notification events for assignments, status changes, escalations, & ratings |
| **API Capabilities** | Pagination & Sorting | Spring Data pagination (`page`, `size`) with multi-column sorting (`createdAt`, `slaDeadline`, etc.) |
| | Search & Filtering | Full keyword search across citizen name, location, and description + status/category filters |
| **UI / UX** | Responsive Civic Portal | Glassmorphic civic interface with interactive visual timelines, responsive tables, & modals |
| **Documentation** | Swagger / OpenAPI 3 | Interactive Swagger UI with full schema annotations at `/swagger-ui.html` |

---

## 🏗️ Technology Stack

- **Backend:** Spring Boot 3.3.4, Java 21, Spring Data JPA, Spring Web MVC, Hibernate, Bean Validation
- **Database:**
  - **Embedded (Default):** H2 in MySQL Compatibility Mode (`MODE=MySQL;DATABASE_TO_LOWER=TRUE`) for instant zero-configuration execution.
  - **Production:** MySQL 8.0+ supported via `application-mysql.properties` with provided `schema.sql` and `data.sql`.
- **API Documentation:** Springdoc OpenAPI 2.6.0 (Swagger 3 UI)
- **Frontend:** Vanilla HTML5, CSS3 (Custom Civic Design Tokens & CSS Variables, Inter Font, Glassmorphism), and Vanilla JavaScript (Fetch API, SPA Router, State Management).

---

## 📂 Project Structure

```
newjavaproject/
├── pom.xml                                    # Maven dependencies & build configuration
├── mvnw.cmd                                   # Maven Wrapper script
├── src/
│   ├── main/
│   │   ├── java/com/grievancetrack/
│   │   │   ├── GrievanceTrackApplication.java # Spring Boot main class
│   │   │   ├── config/                        # DataInitializer, OpenApiConfig, WebConfig
│   │   │   ├── controller/                    # REST Controllers (Grievance, Dept, Cat, Escalation, Rating, Notif, Dashboard)
│   │   │   ├── dto/                           # Request & Response DTOs
│   │   │   ├── entity/                        # JPA Entities (Department, Category, Grievance, Escalation, Rating, Notification)
│   │   │   ├── entity/enums/                  # GrievanceStatus, SlaStatus, NotificationType
│   │   │   ├── exception/                     # GlobalExceptionHandler, Custom exceptions
│   │   │   ├── repository/                    # Spring Data JPA Repositories
│   │   │   ├── scheduler/                     # SlaEscalationScheduler (@Scheduled)
│   │   │   └── service/                       # Business logic services & interfaces
│   │   └── resources/
│   │       ├── application.properties         # Default profile (H2 MySQL-compatibility mode)
│   │       ├── application-mysql.properties   # Production profile for local/cloud MySQL 8
│   │       ├── schema.sql                     # Full MySQL DDL script
│   │       ├── data.sql                       # Seed data DML script
│   │       └── static/                        # Frontend Single Page Application
│   │           ├── index.html                 # 8 Views: Home, Submit, Track, Dashboard, Officer, Admin, SLA Charter
│   │           ├── css/style.css              # Custom Civic Design System
│   │           └── js/app.js                  # Frontend State & REST Client
│   └── test/
│       └── java/com/grievancetrack/
│           ├── GrievanceTrackApplicationTests.java # 20 integration test cases
│           └── SlaEscalationSchedulerTest.java    # Automated escalation unit tests
└── README.md
```

---

## 🚦 Getting Started

### Prerequisites
- **JDK 21** or higher installed.
- PowerShell or Terminal.

### 1. Running the Application (Instant Zero-Setup Mode)
The default configuration uses embedded H2 configured in MySQL compatibility mode. No database installation is required:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on **`http://localhost:8080/`**.

### 2. Accessing the Application
- **Civic Portal UI:** [http://localhost:8080/](http://localhost:8080/)
- **Swagger UI API Docs:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3 JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:grievancetrack_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE`
  - User: `root` | Password: *(blank)*

### 3. Running with MySQL Server
To run with a native MySQL database:
1. Ensure MySQL is running on port `3306` with a database named `grievancetrack_db`.
2. Execute the provided `src/main/resources/schema.sql` and `src/main/resources/data.sql` scripts in MySQL.
3. Start the application with the MySQL Spring profile:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 4. Running the Automated Test Suite
To execute all 20 integration and unit test scenarios:
```powershell
.\mvnw.cmd test
```

---

## 📡 REST API Reference

### 1. Grievance Management (`/api/grievances`)
- `POST /api/grievances` — Lodge complaint (validates request, calculates SLA, routes department, generates code).
- `GET /api/grievances` — Paginated and sorted list (`page`, `size`, `sort`, `search`, `status`, `categoryId`, `departmentId`).
- `GET /api/grievances/{id}` — Get complaint by ID.
- `GET /api/grievances/code/{code}` — Track complaint by code (e.g. `GRV-000001`).
- `PUT /api/grievances/{id}/status` — Perform status transition (`PENDING` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED`/`ESCALATED`/`REJECTED`).
- `DELETE /api/grievances/{id}` — Delete grievance.

### 2. Citizen Ratings (`/api/grievances/{id}/rating`)
- `POST /api/grievances/{id}/rating` — Submit 1–5 star rating for resolved grievance (rejects duplicates with `409 Conflict`).
- `GET /api/grievances/{id}/rating` — View submitted rating for grievance.

### 3. Categories & SLAs (`/api/categories`)
- `GET /api/categories` — List all categories with fixed SLA turnaround days and departments.
- `POST /api/categories` — Add category.
- `PUT /api/categories/{id}` — Update category name and SLA days.
- `DELETE /api/categories/{id}` — Delete category.

### 4. Departments (`/api/departments`)
- `GET /api/departments` — List departments and contact information.
- `POST /api/departments` — Register new department.
- `PUT /api/departments/{id}` — Update department details.
- `DELETE /api/departments/{id}` — Remove department.

### 5. SLA Escalations (`/api/escalations`)
- `GET /api/escalations` — Audit log of all escalated complaints with designated Senior Officer and reason.

### 6. Notifications (`/api/notifications`)
- `GET /api/notifications` — Retrieve civic notifications (optional `grievanceId` filter).
- `PUT /api/notifications/{id}/read` — Mark notification as read.
- `PUT /api/notifications/read-all` — Mark all notifications as read.

### 7. Dashboard Analytics (`/api/dashboard/stats`)
- `GET /api/dashboard/stats` — Real-time municipal KPIs, status counts, SLA compliance percentage, and category breakdown.

---

## 🧪 Verified Test Scenarios

The comprehensive test suite in `GrievanceTrackApplicationTests` and `SlaEscalationSchedulerTest` verifies:
1. **Complaint Creation:** Automatic code generation, department auto-routing, SLA deadline math.
2. **Input Validation:** Validation errors on missing citizen name, invalid email, empty description.
3. **Valid Transitions:** `PENDING` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED`.
4. **Invalid Transitions:** `PENDING` ➔ `RESOLVED` directly returns `400 Bad Request`.
5. **Post-Resolution Rating:** Citizen rating recorded with average rating computation.
6. **Duplicate Rating Rejection:** Duplicate rating on same grievance rejected with `409 Conflict`.
7. **Premature Rating Rejection:** Rating an in-progress complaint rejected with `400 Bad Request`.
8. **Automated Escalation:** Overdue complaint escalated by scheduler, senior officer assigned, and notification sent.
9. **Duplicate Escalation Guard:** Escalation engine prevents re-escalating an already escalated complaint.
10. **Pagination & Sorting:** Paged responses with dynamic sort criteria (`slaDeadline,asc`, `createdAt,desc`).
11. **Keyword Search & Filter:** Search across code, citizen, description, location.

---

## 🏛️ License
Developed as an official civic grievance redressal assessment project.
