# travel-app

# Travel Booking Platform

A cloud-native travel booking platform built using Java, Spring Boot, and Spring Cloud. The platform allows users to browse tours, reserve seats, make payments, and manage bookings using a microservices architecture.

---

## Features

* User registration and management
* Tour inventory management
* Tour booking workflow
* Payment processing
* Service discovery using Eureka
* API Gateway routing
* Inter-service communication using OpenFeign
* Distributed transaction handling using Saga Pattern (planned)
* Event-driven architecture using Kafka (planned)
* Caching with Redis (planned)
* Resilience patterns using Resilience4j (planned)

---

## Architecture

```text
                    +----------------+
                    |   API Gateway  |
                    +-------+--------+
                            |
                    +-------+--------+
                    | Eureka Server  |
                    +-------+--------+
                            |
        ---------------------------------------------
        |               |              |            |
        |               |              |            |
+-------v------+ +------v------+ +-----v------+ +---v------+
| User Service | | Tour Service| | Booking   | | Payment  |
|              | |             | | Service   | | Service  |
+--------------+ +-------------+ +------------+ +----------+
```

---

## Services

### User Service

Responsible for:

* User registration
* User lookup by ID
* User lookup by email

#### Endpoints

```http
POST /users
GET /users/{id}
GET /users/email/{email}
```

---

### Tour Service

Responsible for:

* Creating tours
* Managing tour inventory
* Reserving seats
* Releasing seats

#### Endpoints

```http
POST /tours
GET /tours/{id}
GET /tours
POST /tours/{id}/reserve
POST /tours/{id}/release
```

---

### Booking Service

Responsible for:

* Creating bookings
* Booking validation
* Booking status management
* Orchestrating booking workflow

#### Booking Statuses

```text
PENDING
CONFIRMED
FAILED
CANCELLED
```

#### Endpoints

```http
POST /bookings
GET /bookings/{id}
GET /bookings?userId={id}
POST /bookings/{id}/cancel
```

---

### Payment Service

Responsible for:

* Processing payments
* Refunding payments
* Tracking payment status

#### Payment Statuses

```text
SUCCESS
FAILED
REFUNDED
```

#### Endpoints

```http
POST /payments
GET /payments/{bookingId}
POST /payments/{bookingId}/refund
```

---

## Booking Workflow

```text
Create Booking Request
        |
        v
Reserve Tour Seats
        |
        v
Process Payment
        |
        v
Payment Successful?
     /        \
   YES         NO
    |           |
    v           v
Booking     Release Seats
Confirmed        |
                 v
           Booking Failed
```

This workflow prevents overselling and demonstrates a Saga-style compensation pattern.

---

## Technology Stack

| Category          | Technology             |
| ----------------- | ---------------------- |
| Language          | Java 21                |
| Framework         | Spring Boot 3          |
| Service Discovery | Netflix Eureka         |
| API Gateway       | Spring Cloud Gateway   |
| Communication     | OpenFeign              |
| Database          | MySQL                  |
| Build Tool        | Maven                  |
| Containerization  | Docker (Planned)       |
| Messaging         | Kafka (Planned)        |
| Cache             | Redis (Planned)        |
| Resilience        | Resilience4j (Planned) |

---

## Running the Project

### Prerequisites

* Java 21
* Maven
* MySQL
* Docker (future enhancement)

### Start Infrastructure

1. Start MySQL
2. Start Eureka Server

### Start Services

```bash
# Start Eureka
mvn spring-boot:run

# Start User Service
mvn spring-boot:run

# Start Tour Service
mvn spring-boot:run

# Start Payment Service
mvn spring-boot:run

# Start Booking Service
mvn spring-boot:run

# Start API Gateway
mvn spring-boot:run
```

### Verify Eureka Registration

Open:

```text
http://localhost:8761
```

You should see all services registered successfully.

---

## Database Design

Each microservice owns its own database schema.

```text
mysql
│
├── user_db
├── tour_db
├── booking_db
└── payment_db
```

This ensures service isolation and follows microservice best practices.

---

## Future Enhancements

### Distributed Transactions

* Saga Pattern
* Compensating actions
* Reservation timeout handling

### Event-Driven Architecture

* Kafka Producer/Consumer
* Booking Created Events
* Payment Processed Events
* Notification Events

### Caching

* Redis for frequently accessed tour data
* Reduced database load
* Improved response times

### Resilience

* Retry policies
* Circuit Breakers
* Fallback mechanisms
* Timeout handling

### Security

* JWT Authentication
* Role-based access control
* API Gateway authorization

### Observability

* Prometheus
* Grafana
* Distributed tracing

### Deployment

* Docker Compose
* Kubernetes (future)
* CI/CD pipeline

---

## Learning Objectives

This project was built to gain hands-on experience with:

* Microservice Architecture
* Spring Cloud Ecosystem
* Service Discovery
* API Gateway Patterns
* Distributed Transactions
* Event-Driven Systems
* Resilience Patterns
* Cloud-Native Development
* Backend System Design

---

## Current Status

### Completed

* API Gateway
* Eureka Service Registry
* User Service
* Tour Service
* Booking Service
* Payment Service
* OpenFeign Integration
* Booking Workflow
* Seat Reservation Logic
* Payment Compensation Logic

### In Progress

* Saga Pattern
* Kafka Integration
* Redis Caching
* Resilience4j
* Dockerization

---

## Author

Backend microservices project built to strengthen expertise in distributed systems, cloud-native applications, and modern Java backend development.
