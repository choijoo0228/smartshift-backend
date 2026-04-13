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

## Required Software / Dependencies

### Development Requirements:
* Java 21
* Gradle
* PostgreSQL (for local development)
* Git
* IntelliJ IDEA or any Java IDE (optional)

### Main Frameworks / Libraries:
* Spring Boot
* Spring Web
* Spring Data JPA
* PostgreSQL JDBC Driver
* Spring Boot Actuator
* BCrypt password hashing
* roster-engine custom library (published through JitPack)

### Cloud Services Used:
* Amazon EC2
* Amazon RDS (PostgreSQL)
* Application Load Balancer (ALB)
* AWS Certificate Manager (ACM)
* Amazon CloudWatch


---

## Project Structure

```
smartshift-backend
├── src/main/java
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   ├── config
│   └── exception
├── src/main/resources
│   ├── application.properties
│   ├── application-local.properties
│   └── application-prod.properties
├── build.gradle
├── settings.gradle
├── gradlew
└── gradle/
```

External module:

* `roster-engine` (custom scheduling library)

---

## Important Configuration Files

### application.properties
* Base application configuration
* Defines the default active profile

### application-local.properties
* Used for local development
* Contains local PostgreSQL connection details

### application-prod.properties
* Used in production
* Reads database settings from environment variables
* Includes logging configuration and actuator settings

### systemd service file (on EC2)
* /etc/systemd/system/smartshift.service
* Used to run the Spring Boot JAR as a background service

### GitHub Actions workflow files
* .github/workflows/backend-deploy.yml
* .github/workflows/qodana_code_quality.yml
* Used for CI/CD deployment to EC2

---
## Local Development Configuration

Edit application-local.properties with local database values.

Example:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/smartshift
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

If application.properties contains:
spring.profiles.active=local

then local development will use the local database automatically.

---

## Running Locally

* Step 1: Clone the repository

```
git clone https://github.com/choijoo0228/smartshift-backend.git
cd smartshift-backend
```

* Step 2: Configure database

Make sure PostgreSQL is running locally and the smartshift database exists.

* Step 3: Run the application

```
./gradlew bootRun
```

Backend runs at:

```
http://localhost:8080
```
---

## Build the Backend

Build the executable JAR:

./gradlew clean bootJar -x test

The generated JAR file will be located in:

build/libs/

---

## Custom library Dependency

The backend uses a custom Java library called roster-engine.

This library is published via JitPack and added as a Gradle dependency.

Example Gradle configuration:

```
repositories {
mavenCentral()
maven { url 'https://jitpack.io' }
}

dependencies {
implementation 'com.github.choijoo0228:roster-engine:1.0.0'
}

```

Library URL:

```
https://jitpack.io/#choijoo0228/roster-engine
```

---

## Production Deployment Configuration

Production backend is deployed on Amazon EC2.

The application uses environment variables in production.

Example values used in smartshift.service:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<RDS-ENDPOINT>:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<PASSWORD>
```

The production configuration file reads these values:
```
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
logging.file.name=/home/ec2-user/smartshift-backend/app.log
management.endpoints.web.exposure.include=health
```

---

## EC2 Deployment steps

* Step 1: Launch an EC2 instance (Amazon Linux 2023 recommended)

* Step 2: Install required packages on EC2

```
sudo dnf update -y
sudo dnf install -y git java-21-amazon-corretto-devel
```

* Step 3: Clone the backend repository on EC2
```
git clone git@github.com:choijoo0228/smartshift-backend.git
cd smartshift-backend
```
* Step 4: Build the backend JAR
```
./gradlew bootJar -x test
```

* Step 5: Create systemd service file:
```
sudo nano /etc/systemd/system/smartshift.service
```

Example:
```
[Unit]
Description=SmartShift Backend
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/smartshift-backend
ExecStart=/usr/bin/java -jar /home/ec2-user/smartshift-backend/app.jar
SuccessExitStatus=143
Restart=always
RestartSec=5
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://<RDS-ENDPOINT>:5432/postgres
Environment=SPRING_DATASOURCE_USERNAME=postgres
Environment=SPRING_DATASOURCE_PASSWORD=<PASSWORD>

[Install]
WantedBy=multi-user.target
```
Step 6:
Reload and start service
```
sudo systemctl daemon-reload
sudo systemctl restart smartshift
sudo systemctl status smartshift
```
---

## RDS connection

Amazon RDS PostgreSQL is used in production.

Security Group Rules:
- ALB allows incoming traffic from the internet on 80/443
- EC2 allows backend traffic from ALB
- RDS allows PostgreSQL port 5432 only from EC2 security group

The EC2 backend connects to RDS using the configured JDBC URL.

---

## ALB / Domain / HTTPS

Backend API is exposed through:
```
https://api.choijoo.dev\
```

Application Load Balancer routes incoming traffic to EC2.
AWS Certificate Manager is used to provide HTTPS certificate for the backend domain.

Health check endpoint:
```
http://localhost:8080/actuator/health
```

---

## CloudWatch Logging

Spring Boot logs are written to:
```
/home/ec2-user/smartshift-backend/app.log
```

CloudWatch agent is configured on EC2 to stream backend logs to Amazon CloudWatch.

---

## CI/CD Deployment

GitHub Actions is used for backend CI/CD.

Main workflow:
* checkout repository
* setup Java 21
* build Spring Boot JAR
* connect to EC2 using SSH
* copy app.jar to EC2
* restart smartshift service

Additionally, a Qodana workflow is used to perform automated code quality checks as part of the CI pipeline.

Workflow file:
```
.github/workflows/backend-deploy.yml
.github/workflows/qodana_code_quality.yml
```

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

## Known Limitations

* AWS SES integration was designed but not fully used in deployment due to Learner Lab IAM restrictions
* CloudFront and Amplify were tested/planned but not used due to restricted permissions
* Full JWT authentication is future work
* Some security features are simplified for project scope

---

## Author

Choijoo Erdenesuren

Student ID: 25116380

National College of Ireland

MSc in Cloud Computing
