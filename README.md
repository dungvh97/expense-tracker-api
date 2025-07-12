# 💸 Expense Tracker API

Spring Boot backend API for tracking personal expenses.  
Built with Java, Spring Boot 3, and PostgreSQL.

## 🔧 Tech Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven
- Docker + Docker Compose
- Swagger (OpenAPI)
- Lombok

## 🚀 Features

### ✅ Week 1
- [x] Setup Spring Boot project
- [x] Connect to PostgreSQL
- [x] REST API `/api/hello`

### ✅ Week 2
- [x] Register/Login with JWT
- [x] Secure endpoints with JWT + Spring Security

### ✅ Week 3
- [x] Expense CRUD
- [x] Budget management
- [x] Filter by category/date range

### ✅ Week 4
- [x] Statistics (monthly/category)
- [x] Export to CSV/Excel
- [x] Global Exception Handling
- [x] Swagger API documentation
- [x] Logging to file (request/response/errors)

### ✅ Week 5
- [x] Dockerize Spring Boot app + PostgreSQL
- [x] Run with Docker Compose
- [x] Push Docker image to Docker Hub: [`dungvh97/expense-tracker-api`](https://hub.docker.com/repository/docker/dungvh97/expense-tracker-api)
- [x] Add running instructions to README

## 📦 Project Structure

```
src
└── main
    ├── java
    │   └── com.nvd.expensetracker
    │       ├── auth
    │       ├── config
    │       ├── controller
    │       ├── dto
    │       ├── exception
    │       ├── logging
    │       ├── model
    │       ├── query
    │       ├── repository
    │       ├── service
    │       └── ExpenseTrackerApiApplication.java
    └── resources
        ├── application.properties
        └── logback-spring.xml
```

## ✅ How to Run

### Option 1: Run locally with Maven

```bash
./mvnw spring-boot:run
```

Access:
- API: `http://localhost:8080/api/hello`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

### Option 2: Run with Docker Compose

```bash
docker compose up --build
```

> Make sure Docker is installed and running. This will start both the app and PostgreSQL.

## 🔐 Authentication

- Register: `POST /api/auth/register`
- Login: `POST /api/auth/login`
- Add `Authorization: Bearer <token>` to call secured endpoints

## 📂 Export Features

- `GET /api/expenses/export/csv` → CSV file
- `GET /api/expenses/export/excel` → Excel file

## 📝 Author

Dung Nguyen – [LinkedIn](https://www.linkedin.com/in/dung-nguyen-qt/)
