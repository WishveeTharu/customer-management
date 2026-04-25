# Customer Management System

A full-stack web application for managing customers, built with Spring Boot and React JS.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 8, Spring Boot 2.7.18, Maven |
| Frontend | React JS, Axios |
| Database | MariaDB 11.4 |
| Testing | JUnit 5, Mockito |
| Excel Processing | Apache POI, Excel Streaming Reader |

---

## Features

- ✅ Create a customer
- ✅ Update a customer
- ✅ View a single customer
- ✅ View all customers in table view with search and pagination
- ✅ Multiple mobile numbers per customer
- ✅ Multiple addresses per customer (with city/country)
- ✅ Family member linking between customers
- ✅ Bulk customer create/update via Excel file (supports 1,000,000+ records)
- ✅ NIC number uniqueness validation
- ✅ Input validation (NIC: 12 digits, Mobile: 10 digits)

---

## Prerequisites

Make sure the following are installed before running the project:

| Tool | Version | Download |
|------|---------|----------|
| Java (Temurin) | 8 (1.8.0_482) | https://adoptium.net |
| Maven | 3.9.x | https://maven.apache.org |
| MariaDB | 11.4 | https://mariadb.org/download |
| Node.js | 18+ | https://nodejs.org |
| npm | 9+ | Included with Node.js |

---

## Project Structure

```
customer-management/
├── backend/                          # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/customer/
│   │   │   │   ├── config/           # CORS configuration
│   │   │   │   ├── controller/       # REST controllers
│   │   │   │   ├── dto/              # Data transfer objects
│   │   │   │   ├── model/            # JPA entities
│   │   │   │   ├── repository/       # Spring Data repositories
│   │   │   │   ├── service/          # Business logic
│   │   │   │   └── CustomerManagementApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── ddl.sql           # Database schema
│   │   │       └── dml.sql           # Seed data
│   │   └── test/                     # JUnit tests
│   └── pom.xml
└── frontend/                         # React application
    ├── src/
    │   ├── pages/
    │   │   ├── CustomerList.js       # Table view with search
    │   │   ├── CustomerForm.js       # Create/Edit form
    │   │   ├── CustomerView.js       # View single customer
    │   │   └── BulkUpload.js         # Excel bulk upload
    │   ├── services/
    │   │   └── api.js                # Axios API calls
    │   ├── App.js
    │   └── index.js
    └── package.json
```

---

## Database Setup

### Step 1 — Start MariaDB

Make sure MariaDB is running on port **3307**.

### Step 2 — Run DDL Script (Create Tables)

```bash
mysql -u root -p --port=3307 < backend/src/main/resources/ddl.sql
```

### Step 3 — Run DML Script (Seed Data)

```bash
mysql -u root -p --port=3307 < backend/src/main/resources/dml.sql
```

Enter password: `root`

### Database Schema

| Table | Description |
|-------|-------------|
| `customer` | Core customer records |
| `customer_mobile` | Multiple mobile numbers per customer |
| `customer_address` | Multiple addresses per customer |
| `customer_family` | Family member relationships |
| `country` | Master data - countries |
| `city` | Master data - cities |

---

## Running the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**

To run with more memory (recommended for bulk upload):
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms512m -Xmx4g"
```

### Backend API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | Get all customers (paginated) |
| GET | `/api/customers/{id}` | Get customer by ID |
| POST | `/api/customers` | Create new customer |
| PUT | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |
| POST | `/api/customers/bulk-upload` | Bulk upload via Excel |
| GET | `/api/master/countries` | Get all countries |
| GET | `/api/master/cities?countryId={id}` | Get cities by country |

---

## Running the Frontend

```bash
cd frontend
npm install
npm start
```

The frontend starts on **http://localhost:3000**

> ⚠️ Make sure the backend is running first before starting the frontend.

---

## Running Tests

```bash
cd backend
mvn test
```

Tests cover:
- Create customer (success and duplicate NIC)
- Get customer by ID (success and not found)
- Get all customers (with and without search)
- Update customer (success and not found)
- Delete customer (success and not found)

---

## Bulk Upload

### Excel File Format

| Column A | Column B | Column C |
|----------|----------|----------|
| Name | Date of Birth | NIC Number |
| Amal Perera | 1998-05-15 | 199812345678 |

- Supported date formats: `yyyy-MM-dd`, `dd/MM/yyyy`, `MM/dd/yyyy`
- Supports up to **1,000,000+ records**
- Uses streaming reader for memory efficiency
- Duplicate NICs are updated (upsert)
- Max file size: **500MB**

---

## Configuration

`backend/src/main/resources/application.properties`

```properties
# Database
spring.datasource.url=jdbc:mariadb://localhost:3307/customer_db
spring.datasource.username=root
spring.datasource.password=root

# File Upload
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Bulk Processing
app.bulk.batch-size=1000
```

---

## Validation Rules

| Field | Rule |
|-------|------|
| Name | Required |
| Date of Birth | Required, must be a valid date |
| NIC Number | Required, exactly 12 digits, must be unique |
| Mobile Number | Optional, exactly 10 digits each |
| Address Line 1 | Required if address is added |
| City & Country | Required if address is added |

---

## Notes

- Cities and Countries are stored in master data tables and managed via backend only
- Family members are linked bidirectionally
- The bulk upload uses JDBC batch inserts for performance
- All API responses use JSON format
- CORS is configured to allow requests from `localhost:3000`

---

## Author

Tharu Matharage — Software Engineer Internship Assignment