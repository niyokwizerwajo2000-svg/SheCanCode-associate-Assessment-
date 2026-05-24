# Idempotency Gateway - The "Pay-Once" Protocol

## Architecture Diagram
## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+

### Run the application

```bash
git clone https://github.com/niyokwizerwajo2000-svg/SheCanCode-associate-Assessment-.git
cd SheCanCode-associate-Assessment-
mvn spring-boot:run
```

Server starts on: `http://localhost:8080`

## API Documentation

### POST /process-payment

**Request Headers:**
- `Idempotency-Key: any-unique-string` (required)
- `Content-Type: application/json` (required)

**Request Body:**
```json
{
  "amount": 100,
  "currency": "RWF"
}
```

**Response 1 - New request (201):**
```json
{
  "message": "Charged 100 RWF",
  "transactionId": "uuid-here",
  "status": "success"
}
```

**Response 2 - Duplicate request (201):**

**Response 3 - Same key, different body (422):**
```json
{
  "error": "Idempotency key already used for a different request body."
}
```

## Design Decisions

1. **ConcurrentHashMap** - Thread-safe in-memory store. No external database needed for this implementation.

2. **2-Second Delay** - Simulates real payment processing using Thread.sleep(2000).

3. **Spring Boot** - Chosen for rapid development, embedded Tomcat, and production-ready features.

## Developer's Choice Feature

**Request Expiry (TTL - 24 hours)**

I added automatic expiry of idempotency keys after 24 hours.

Why: Real Fintech companies like Stripe expire keys after 24 hours to prevent memory leaks and follow industry standards.

## Pre-Submission Checklist

- [x] Repository is Public
- [x] No unnecessary files committed
- [x] Server starts with `mvn spring-boot:run`
- [x] Architecture Diagram included
- [x] API Documentation included
- [x] Multiple meaningful commits