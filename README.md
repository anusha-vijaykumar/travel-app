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
* **Idempotent Kafka consumers** - Exactly-once semantics with event deduplication
* Transactional outbox pattern for reliable Kafka publishing
* Resilience patterns using Resilience4j
* Distributed event tracking for idempotency guarantee

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

## Idempotent Kafka Consumers

The project implements **exactly-once semantic** message processing despite Kafka's at-least-once delivery guarantee through event deduplication.

### How It Works

1. **Event ID Generation**: Each event carries a unique UUID (`eventId`) from source to consumer
2. **Duplicate Detection**: Consumers check the `processed_events` table before processing
3. **Atomic Processing**: Consumer operations (check + process + record) happen in a single database transaction
4. **Replay Safety**: If a message is replayed, the duplicate check prevents repeated business logic execution

### Consumer Implementation

**Booking Service - PaymentResultConsumer**:
- Consumes payment results from `payment-result-topic`
- Checks if `PaymentResultEvent.eventId` was already processed
- Updates booking status if new event; skips if duplicate

**Payment Service - PaymentConsumer**:
- Consumes payment requests from `booking-topic`
- Checks if `PaymentEvent.eventId` was already processed
- Creates payment record if new event; skips if duplicate

### Without This: What Could Go Wrong

- **Duplicate Bookings**: Message replayed → 2 identical bookings created
- **Double Charges**: Payment processed twice due to Kafka rebalancing
- **Inconsistent State**: Some services processed event, others didn't

### With This: Guaranteed Safety

- First delivery → processed normally
- All subsequent deliveries of same message → safely ignored
- System state remains consistent regardless of message replays

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
|   |-- booking
|   |-- outbox_event
|   `-- processed_events (for idempotency)
`-- payment database
    |-- payment
    |-- outbox_event
    |-- dead_letter_payment_event
    `-- processed_events (for idempotency)
```

This keeps service data isolated and allows each service to publish its own reliable integration events.

### Idempotency Tracking

The `processed_events` table in Booking Service and Payment Service tracks successfully processed Kafka messages:

- **event_id (PRIMARY KEY)**: Uniquely identifies each event (UUID)
- **processed_at**: Timestamp when the event was processed
- Prevents duplicate processing by using database constraint enforcement

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
* **Event deduplication and idempotent consumers**
* **Exactly-once semantics with at-least-once delivery**
* Saga-style workflow design
* Resilience patterns
* Backend system design
* Database per service pattern

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
* Booking workflow with full transaction lifecycle
* Seat reservation and release logic
* Kafka payment request and payment result topics
* Booking outbox publisher with retry logic
* Payment outbox publisher with retry logic
* Payment result consumer that updates booking status
* **Idempotent Kafka consumers** with event deduplication
* ProcessedEvents table for duplicate detection in both Payment and Booking services
* JPA entity-based database schema management (Hibernate ddl-auto)
* Service-level unit tests (32+ tests passing)
* Dead letter queue for failed payment events

### Architectural Decisions

**Schema Management**: All services use JPA entities with Hibernate's `ddl-auto: update` for schema management. This provides consistency across the codebase - all tables (business entities and idempotency tracking tables) are defined as JPA entities rather than manual SQL migrations.

### In Progress

* Notification Service enhancements
* Redis caching for frequently accessed tour data
* Dockerization with Docker Compose
* Production-grade observability (Prometheus, Grafana, distributed tracing)

---

## Author

Backend microservices project built to strengthen expertise in distributed systems, cloud-native applications, and modern Java backend development.
