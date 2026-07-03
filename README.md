# Travel Booking Platform

A cloud-native travel booking platform built with Java, Spring Boot, and Spring Cloud. The platform lets users browse tour inventory, reserve seats, process payments, and manage bookings through a microservices architecture.

---

## Features

* User registration and lookup
* Tour inventory management
* Booking workflow with seat reservation
* Payment processing and refunds
* Service discovery using Eureka
* API Gateway routing
* Inter-service communication using OpenFeign
* Event-driven payment workflow using Kafka
* Transactional outbox pattern for reliable Kafka publishing
* Resilience patterns using Resilience4j

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
+-------v------+ +------v------+ +-----v------+ +---v------+
| User Service | | Inventory   | | Booking   | | Payment  |
|              | | Service     | | Service   | | Service  |
+--------------+ +-------------+ +------------+ +----------+
                                      |              ^
                                      | booking-topic|
                                      v              |
                                +-----+--------------+-----+
                                |          Kafka           |
                                +-----+--------------+-----+
                                      ^              |
                                      | payment-result-topic
                                      |              v
                                +-----+--------------+
                                | Booking Service    |
                                | result consumer    |
                                +--------------------+
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
POST /api/users
GET /api/users/{id}
GET /api/users/email/{email}
```

---

### Inventory Service

Responsible for:

* Creating tour inventory
* Looking up inventory by ID or tour package ID
* Reserving seats
* Releasing seats when compensation is needed

#### Endpoints

```http
POST /api/inventories
GET /api/inventories/{id}
GET /api/inventories/by-tour/{tourPackageId}
POST /api/inventories/reserve?tourPackageId={id}&seats={count}
POST /api/inventories/release?tourPackageId={id}&seats={count}
```

---

### Booking Service

Responsible for:

* Creating bookings
* Validating requested payment amount against inventory price
* Reserving seats through Inventory Service
* Persisting booking payment requests to an outbox table
* Publishing payment request events to Kafka
* Consuming payment result events and updating booking status

#### Booking Statuses

```text
PENDING
PAYMENT_REQUESTED
CONFIRMED
CANCELLED
PAYMENT_FAILED
```

#### Endpoints

```http
POST /api/bookings
GET /api/bookings/{id}
GET /api/bookings?userId={id}
PUT /api/bookings/{id}/cancel
```

---

### Payment Service

Responsible for:

* Consuming booking payment request events
* Processing payments
* Persisting payment result events to an outbox table
* Publishing payment result events to Kafka
* Refunding successful payments
* Tracking payment status

#### Payment Statuses

```text
SUCCESS
FAILED
REFUNDED
```

#### Endpoints

```http
POST /api/payments
GET /api/payments/{bookingId}
PUT /api/payments/{bookingId}/refund
```

---

## Booking Workflow

```text
Create Booking Request
        |
        v
Validate Tour Inventory and Amount
        |
        v
Reserve Tour Seats
        |
        v
Save Booking + Booking Outbox Event
        |
        v
Booking Outbox Publisher -> Kafka booking-topic
        |
        v
Payment Service Consumer
        |
        v
Save Payment + Payment Outbox Event
        |
        v
Payment Outbox Publisher -> Kafka payment-result-topic
        |
        v
Booking Service Payment Result Consumer
        |
        v
Update Booking Status
```

If payment succeeds, the booking is marked `CONFIRMED`. If payment fails, the booking is marked `PAYMENT_FAILED`.

---

## Transactional Outbox

The project uses the transactional outbox pattern in both Booking Service and Payment Service.

### Booking Outbox

* Booking creation and `PAYMENT_REQUESTED` outbox insert happen in one database transaction.
* `OutboxPublisher` reads pending booking outbox rows.
* `BookingProducer` publishes `PaymentEvent` messages to `booking-topic`.
* Published rows are marked `PUBLISHED`; failed publishes remain `PENDING` with attempt/error details for retry.

### Payment Outbox

* Payment creation/update and payment result outbox insert happen in one database transaction.
* `OutboxPublisher` reads pending payment outbox rows.
* `PaymentResultProducer` publishes `PaymentResultEvent` messages to `payment-result-topic`.
* Published rows are marked `PUBLISHED`; failed publishes remain `PENDING` with attempt/error details for retry.

Current topics:

```text
booking-topic
payment-result-topic
```

---

## Technology Stack

| Category          | Technology           |
| ----------------- | -------------------- |
| Language          | Java 21              |
| Framework         | Spring Boot          |
| Service Discovery | Netflix Eureka       |
| API Gateway       | Spring Cloud Gateway |
| Communication     | OpenFeign, Kafka     |
| Database          | MySQL                |
| Build Tool        | Maven                |
| Messaging         | Kafka                |
| Reliability       | Transactional Outbox |
| Resilience        | Resilience4j         |
| Containerization  | Docker (Planned)     |
| Cache             | Redis (Planned)      |

---

## Running the Project

### Prerequisites

* Java 21
* Maven
* MySQL
* Kafka running on `localhost:9092`

### Start Infrastructure

1. Start MySQL.
2. Start Kafka.
3. Start Eureka Server.

### Start Services

Run each service from its own directory:

```bash
# Service Registry
cd service-registry
mvn spring-boot:run

# API Gateway
cd ../api-gateway
mvn spring-boot:run

# User Service
cd ../user-service
mvn spring-boot:run

# Inventory Service
cd ../inventory-service
mvn spring-boot:run

# Booking Service
cd ../booking-service
mvn spring-boot:run

# Payment Service
cd ../payment-service
mvn spring-boot:run
```

### Verify Eureka Registration

Open:

```text
http://localhost:8761
```

You should see the services registered successfully.

---

## Database Design

Each microservice owns its own database schema.

```text
mysql
|-- user database
|-- inventory database
|-- booking database
|   `-- outbox_event
`-- payment database
    `-- outbox_event
```

This keeps service data isolated and allows each service to publish its own reliable integration events.

---

## Testing

Run tests for an individual service from that service directory:

```bash
mvn test
```

Example:

```bash
cd payment-service
mvn test
```

---

## Future Enhancements

### Distributed Transactions

* Reservation timeout handling
* Additional compensating actions
* Stronger idempotency around event consumers

### Event-Driven Architecture

* Notification events
* Dead-letter topics
* Outbox cleanup/archival
* Consumer retry policies

### Caching

* Redis for frequently accessed tour data
* Reduced database load
* Improved response times

### Security

* JWT authentication
* Role-based access control
* API Gateway authorization

### Observability

* Prometheus
* Grafana
* Distributed tracing

### Deployment

* Docker Compose
* Kubernetes
* CI/CD pipeline

---

## Learning Objectives

This project was built to gain hands-on experience with:

* Microservice architecture
* Spring Cloud ecosystem
* Service discovery
* API Gateway patterns
* OpenFeign service-to-service calls
* Kafka-based event-driven systems
* Transactional outbox pattern
* Saga-style workflow design
* Resilience patterns
* Backend system design

---

## Current Status

### Completed

* API Gateway
* Eureka Service Registry
* User Service
* Inventory Service
* Booking Service
* Payment Service
* OpenFeign integration
* Booking workflow
* Seat reservation and release logic
* Kafka payment request and payment result topics
* Booking outbox publisher
* Payment outbox publisher
* Payment result consumer that updates booking status
* Service-level unit tests

### In Progress

* Notification Service
* Redis caching
* Dockerization
* Production-grade observability

---

## Author

Backend microservices project built to strengthen expertise in distributed systems, cloud-native applications, and modern Java backend development.
