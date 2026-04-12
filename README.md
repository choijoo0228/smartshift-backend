# SmartShift Backend

## Overview

SmartShift Backend is a Spring Boot application that provides RESTful APIs for managing employees, users, shifts, and weekly roster scheduling. It supports role-based access for **Admin**, **Manager**, and **Employee**, and integrates with a custom scheduling library (`roster-engine`).

---

## Features

* User authentication (login & password change)
* Role-based access (ADMIN, MANAGER, EMPLOYEE)
* Employee management
* Shift creation, update, and deletion
* Weekly roster view
* Publish shifts by week
* Employees can view **published shifts only**
* Custom scheduling logic via `roster-engine` library
* Notification service (mock implementation for lab environment)

---

## Tech Stack

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL (via Amazon RDS in production)
* Gradle
* AWS (EC2, RDS, S3, CloudWatch)

---

## Project Structure

```
smartshift-backend
├── controller # REST API endpoints
├── service # Business logic
├── repository # Data access layer (JPA)
├── entity # Database entities
├── dto # Data transfer objects
├── config # Configuration classes
├── exception # Global exception handling
```

External module:

* `roster-engine` (custom scheduling library)

---

## API Endpoints

### Authentication

* `POST /api/auth/login`
* `POST /api/auth/change-password`
* `POST /api/auth/create-user`

### Employees

* `GET /api/employees`
* `POST /api/employees`
* `PUT /api/employees/{id}`
* `DELETE /api/employees/{id}`

### Shifts

* `GET /api/shifts`
* `GET /api/shifts/week`
* `GET /api/shifts/published/week`
* `POST /api/shifts`
* `PUT /api/shifts/{id}`
* `DELETE /api/shifts/{id}`
* `POST /api/shifts/publish-week`

---

## Running Locally

### 1. Clone the repository

```
git clone https://github.com/choijoo0228/smartshift-backend.git
cd smartshift-backend
```

### 2. Configure database

Update `application.properties`:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/smartshift
spring.datasource.username=username
spring.datasource.password=password
```

### 3. Run the application

```
./gradlew bootRun
```

Backend runs at:

```
http://localhost:8080
```

---

## Deployment (AWS)

The backend is deployed using:

* **Amazon EC2** – hosts the Spring Boot application
* **Amazon RDS** – managed relational database
* **Amazon S3** – frontend hosting / file storage
* **Application Load Balancer (ALB)** Traffic routing
* **AWS Certificate Manager (ACM)** HTTPS/SSL configuration
* **Amazon CloudWatch** – monitoring and logs

---

## Custom Library

A custom Java library was developed to handle scheduling logic:

* `roster-engine`
* Published using JitPack
* Integrated as a dependency in the backend
* Designed for modularity and reuse

---

## Security Notes

* Passwords are hashed using BCrypt
* Role-based access is implemented at the application level
* Spring Security basic configuration enabled
* Full Spring Security configuration is identified as future work

---

## Notifications

* Email notifications are designed using AWS SES
* Due to AWS Learner Lab IAM restrictions, a **mock notification service** is used
* System architecture supports easy future integration with real SES

---

## Future Improvements

* JWT authentication
* Full Spring Security integration
* Real AWS SES email notifications
* Shift swapping between employees
* Leave request management
* Export roster (CSV/PDF)
* Improved UI integration with frontend

---

## Author

Choijoo Erdenesuren
