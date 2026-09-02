# 🚍 Transport ERP & Fleet Management System

> A modern **Transport ERP and Fleet Management System** designed to centralize fleet operations, driver management, trip dispatch, maintenance, fuel, expenses, documents, GPS tracking, notifications, and reporting.

**Phase 1** focuses on establishing the core architecture and foundation for a scalable transport-management platform using a **Spring Boot modular backend** and a **React-based web application**.

---

## 📌 Overview

The Transport ERP is designed for organizations operating transportation fleets such as:

* 🚌 Buses
* 🚛 Trucks
* 🛻 Pickup vehicles
* 🚗 Cars & Cabs

The system provides a centralized platform for managing vehicles, drivers, trips, documents, maintenance, expenses, and operational data.

The architecture is intentionally designed as a **modular monolith**, allowing the system to remain simple to develop and deploy while keeping individual business domains separated. High-volume modules such as GPS ingestion, notifications, or reporting can later be extracted into independent services if required.

---

## ✨ Key Features

### 🚘 Fleet Management

* Vehicle registration and management
* Vehicle type and status tracking
* Vehicle branch/depot assignment
* Odometer tracking
* Fuel information
* Insurance and compliance tracking
* Fitness certificate management
* Permit and PUC expiry tracking
* FASTag information
* Vehicle service history
* Accident history
* Vehicle document management

### 👨‍✈️ Driver Management

* Driver profiles
* Employee codes
* Contact information
* Branch assignment
* Driver status
* Driving licence information
* Emergency contacts
* Driver documents
* Medical/fitness records
* Training records
* Attendance
* Leave management
* Vehicle/trip assignments

### 🛣️ Trip & Dispatch Management

* Trip planning
* Driver assignment
* Vehicle assignment
* Dispatch management
* Route management
* Trip status tracking
* Halt and route-deviation tracking
* Operational trip records

### 📍 GPS & Tracking

* GPS provider integration
* Vehicle location tracking
* GPS data ingestion
* Location summaries
* Future support for real-time tracking through WebSocket/API integrations

### 🔧 Maintenance

* Vehicle maintenance records
* Service history
* Maintenance scheduling
* Vehicle component tracking
* Maintenance-related expenses

### ⛽ Fuel Management

* Fuel transaction tracking
* Fuel consumption monitoring
* Odometer-based fuel analysis
* Fuel expense management

### 💰 Expense Management

* Operational expenses
* Vehicle-related expenses
* Fuel expenses
* Maintenance expenses
* Expense reporting

### 📄 Document Management

* Vehicle documents
* Driver documents
* Document expiry tracking
* Expiry alerts and notifications
* Centralized document storage

### 🔔 Notifications

* Expiry notifications
* Operational alerts
* System notifications
* Extensible notification architecture for SMS/WhatsApp/email integrations

### 📊 Reports & Analytics

* Fleet reports
* Driver reports
* Trip reports
* Maintenance reports
* Fuel reports
* Expense reports
* Operational analytics

---

## 🏗️ System Architecture

```text
                    ┌─────────────────────────┐
                    │      React Web UI       │
                    │      Vite + React       │
                    └────────────┬────────────┘
                                 │
                              REST API
                                 │
                                 ▼
        ┌────────────────────────────────────────────┐
        │             Spring Boot Backend            │
        │                                             │
        │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
        │  │  Fleet   │  │ Drivers  │  │  Trips   │ │
        │  └──────────┘  └──────────┘  └──────────┘ │
        │                                             │
        │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
        │  │   GPS    │  │Maintenance│ │ Expenses │ │
        │  └──────────┘  └──────────┘  └──────────┘ │
        │                                             │
        │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
        │  │Documents │  │   Auth   │  │ Reports  │ │
        │  └──────────┘  └──────────┘  └──────────┘ │
        └───────────────────┬────────────────────────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
        ┌───────────────┐       ┌───────────────┐
        │  PostgreSQL   │       │     Redis     │
        │ Transaction DB│       │ Cache / Data  │
        └───────────────┘       └───────────────┘
                │
                ▼
        ┌───────────────────┐
        │ External Services │
        │ GPS / S3 / Alerts │
        └───────────────────┘
```

---

## 🛠️ Technology Stack

### Backend

| Technology        | Purpose                        |
| ----------------- | ------------------------------ |
| Java 17           | Backend language               |
| Spring Boot 3.2.5 | Application framework          |
| Spring Web        | REST APIs                      |
| Spring Data JPA   | Database persistence           |
| Spring Security   | Authentication & authorization |
| JWT               | Token-based authentication     |
| PostgreSQL        | Primary database               |
| Flyway            | Database migrations            |
| Redis             | Caching / fast-access data     |
| Lombok            | Boilerplate reduction          |
| AWS S3 SDK        | Document/object storage        |
| Spring Actuator   | Application monitoring         |
| Maven             | Build & dependency management  |

The backend's `pom.xml` confirms Java 17, Spring Boot 3.2.5, PostgreSQL, Redis, Flyway, JWT, AWS S3, Spring Security, and Actuator dependencies.

### Frontend

| Technology      | Purpose             |
| --------------- | ------------------- |
| React 19        | UI framework        |
| Vite            | Frontend build tool |
| Axios           | API communication   |
| React Router    | Application routing |
| React Hot Toast | Notifications       |
| React Icons     | UI icons            |
| Oxlint          | Code linting        |

The frontend is located in `transport-erp-ui` and uses React, Vite, Axios, React Router, React Hot Toast, and React Icons.

### Development & Testing

* Docker / Docker Compose
* Maven
* npm
* Postman
* Git
* VS Code / IntelliJ IDEA

---

## 📁 Project Structure

```text
ERP/
│
├── transport-erp/
│   ├── src/
│   ├── pom.xml
│   ├── docker-compose.yml
│   ├── Transport_ERP_API.postman_collection.json
│   └── ...
│
├── transport-erp-ui/
│   ├── public/
│   ├── src/
│   ├── package.json
│   ├── vite.config.js
│   └── ...
│
├── uploads/
│   └── documents/
│
├── transport_erp_system_design.md
│
└── README.md
```

The current Phase 1 branch contains separate `transport-erp` and `transport-erp-ui` applications, an uploads directory, and a detailed system-design document.

---

# 🚀 Getting Started

## Prerequisites

Make sure the following are installed:

* **Java 17+**
* **Maven 3.8+**
* **Node.js 18+**
* **npm**
* **PostgreSQL**
* **Redis**
* **Git**
* **Docker & Docker Compose** *(recommended)*

---

## 1. Clone the Repository

```bash
git clone https://github.com/ujjwa1Sinha/ERP.git
cd ERP
```

Switch to the Phase 1 branch:

```bash
git checkout phase-1
```

---

# 🔧 Backend Setup

Navigate to the backend:

```bash
cd transport-erp
```

### Install dependencies

```bash
mvn clean install
```

### Configure the database

Create a PostgreSQL database for the application.

Example:

```sql
CREATE DATABASE transport_erp;
```

Configure the required database, JWT, Redis, and storage properties in your Spring Boot configuration.

> Do not commit passwords, JWT secrets, cloud credentials, or other sensitive configuration values to GitHub.

### Run the backend

```bash
mvn spring-boot:run
```

The backend will start using the Spring Boot application configuration.

---

# 🎨 Frontend Setup

Open another terminal and navigate to:

```bash
cd transport-erp-ui
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The Vite development server will display the local URL in the terminal.

### Production Build

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

### Lint

```bash
npm run lint
```

---

# 🐳 Docker

The backend includes a `docker-compose.yml` for containerized infrastructure.

From the backend directory:

```bash
cd transport-erp
docker compose up -d
```

To stop the containers:

```bash
docker compose down
```

Docker is recommended for local development because it simplifies running supporting services such as PostgreSQL and Redis.

---

# 🔐 Authentication

The backend uses **Spring Security with JWT-based authentication**.

The general authentication flow is:

```text
User
 │
 ▼
Login API
 │
 ▼
Authentication
 │
 ▼
JWT Token
 │
 ▼
Frontend
 │
 ▼
Authorization Header
 │
 ▼
Protected REST APIs
```

Protected endpoints should be accessed using a valid JWT token.

---

# 🧪 API Testing

A Postman collection is included in the backend:

```text
transport-erp/
└── Transport_ERP_API.postman_collection.json
```

Import the collection into Postman to test the available REST APIs.

Recommended testing flow:

1. Start PostgreSQL and Redis.
2. Start the Spring Boot backend.
3. Import the Postman collection.
4. Authenticate through the login endpoint.
5. Use the generated JWT for protected endpoints.
6. Test fleet, driver, trip, and other APIs.

---

# 📚 System Design

A detailed system-design document is included in the repository:

```text
transport_erp_system_design.md
```

It describes the planned architecture and domains including:

* Organization & branches
* Fleet management
* Driver management
* Trip & dispatch
* GPS integration
* Maintenance
* Fuel
* Expenses
* Documents
* Notifications
* Reporting
* Authentication

The design recommends starting with a modular monolith and extracting high-volume components into separate services only when scale requires it.

---

# 🔒 Security Considerations

The application is designed with security as a core requirement.

Key considerations include:

* JWT-based authentication
* Role-based authorization
* Password security through Spring Security
* Protected REST endpoints
* Validation of API inputs
* Secure document storage
* Separation of sensitive driver information
* Environment-based secret management

Sensitive information such as driver medical records should only be accessible to authorized roles.

---

# 🤝 Contributing

Contributions are welcome.

### Development workflow

```bash
# Create a feature branch
git checkout -b feature/your-feature

# Make your changes
git add .

# Commit
git commit -m "feat: add your feature"

# Push
git push origin feature/your-feature
```

Then open a Pull Request against the appropriate branch.

### Commit Convention

Recommended commit prefixes:

```text
feat:     New feature
fix:      Bug fix
docs:     Documentation
refactor: Code refactoring
test:     Tests
chore:    Maintenance
```

Example:

```text
feat: add vehicle management API
fix: resolve driver assignment validation
docs: update API documentation
```

---

# 👨‍💻 Project

**Transport ERP / Fleet Management System**

Repository:
https://github.com/ujjwa1Sinha/ERP

Current development branch:

```text
phase-1
```

---

## ⭐ Project Vision

The long-term goal is to build a unified platform that gives transport organizations a **single source of truth for their fleet and operations**.

From vehicles and drivers to trips, GPS tracking, maintenance, fuel, documents, expenses, notifications, and analytics — the Transport ERP aims to bring the complete operational lifecycle into one scalable platform.

> **Manage the fleet. Track the operations. Control the costs.**
