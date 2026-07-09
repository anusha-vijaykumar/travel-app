# Docker Infrastructure

This Compose stack provides the local infrastructure used by the Spring microservices:

- MySQL on `localhost:3306`
- Kafka on `localhost:9092`
- Kafka UI on `http://localhost:8090`
- Zipkin on `http://localhost:9411`
- MailHog SMTP on `localhost:1025` and UI on `http://localhost:8025`
- Prometheus on `http://localhost:9090`
- Grafana on `http://localhost:3000` with `admin` / `admin`

Start everything:

```powershell
docker compose up -d
```

The Compose stack includes Kafka and Zipkin. If another local Kafka or Zipkin is already using `9092` or `9411`, stop that existing process/container first so Compose can own the full infrastructure.

Stop everything:

```powershell
docker compose down
```

The MySQL container creates these databases on first startup:

- `travel_app_user_db`
- `travel_app_inventory_db`
- `travel_app_booking_db`
- `travel_app_payment_db`

Prometheus runs inside Docker and scrapes the Spring services through `host.docker.internal`. Start the Spring services on their normal local ports, then check:

```text
http://localhost:9090/targets
```

Kafka topics are created automatically by the `kafka-init` service:

- `booking-topic`
- `payment-result-topic`
- `payment-dead-letter-topic`
