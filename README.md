# Subscription Billing Management System

A Spring Boot REST API for managing customers, subscription plans, subscriptions, invoices, and simulated payment records. This project corresponds to **Project 7: Subscription Billing Management System** in the supplied *Student-Ready Project Statements* PDF.

> **Implementation note:** This README documents the code in the supplied `billingsystem` ZIP. It does not claim features that are not currently implemented. The project is a learning/demo system; payment processing is simulated and must not be used for real transactions.

## Contents

- [Overview](#overview)
- [Implemented functionality](#implemented-functionality)
- [Technology stack](#technology-stack)
- [Architecture](#architecture)
- [Data model](#data-model)
- [API reference](#api-reference)
- [Business rules currently implemented](#business-rules-currently-implemented)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Run locally](#run-locally)
- [Example requests](#example-requests)
- [Error handling](#error-handling)
- [Testing](#testing)
- [Known limitations and recommended improvements](#known-limitations-and-recommended-improvements)
- [Security and payment safety](#security-and-payment-safety)
- [Project structure](#project-structure)

## Overview

Subscription services need a consistent way to define plans, enrol customers, track subscription dates and statuses, issue invoices for billing periods, and record payment outcomes. This API models that workflow with five persistent entities and a layered Spring application.

### Actors

- **Customer:** represented by a customer record; can be associated with subscriptions.
- **Service administrator/operator:** in the current version, administrative actions are exposed as API endpoints without authentication or role checks.

## Implemented functionality

- Create customers.
- Create and list subscription plans.
- Create subscriptions using a customer ID and plan ID.
- Derive initial renewal and expiry dates from the plan billing interval (`MONTHLY` or `YEARLY`).
- Create an initial `PENDING` invoice when a subscription is created.
- List a customer's subscriptions.
- Cancel a subscription by setting its status to `CANCELLED`.
- Renew a subscription when the current date is on or after its next renewal date.
- List invoices for a subscription.
- Record simulated payment outcomes (`SUCCESS` or `FAILED`) and list payment records for an invoice.
- Return structured error responses through a global exception handler.

## Technology stack

- Java (project uses the Spring Boot parent version specified in `pom.xml`)
- Spring Boot / Spring Web
- Spring Data JPA and Hibernate
- Jakarta Persistence and Jakarta Validation
- PostgreSQL
- Maven / Maven Wrapper
- JUnit 5 and Spring Boot Test

Check `pom.xml` for the exact dependency and Java versions.

## Architecture

The code follows a Controller–Service–Repository–Entity structure:

1. **Controller layer** maps HTTP routes and returns HTTP responses.
2. **Service layer** applies business rules and coordinates persistence.
3. **Repository layer** uses Spring Data JPA repositories.
4. **Entity layer** defines the relational model persisted by Hibernate.
5. **Exception layer** centralises common API errors.

```text
HTTP client (Postman/curl)
        |
        v
Controllers
(Customer / Plan / Subscription / Payment)
        |
        v
Services
(Customer / Plan / Subscription / Invoice / Payment)
        |
        v
Spring Data JPA repositories
        |
        v
Hibernate ORM
        |
        v
PostgreSQL database
```

## Data model

The application has these five JPA entities:

### `Customer`

| Column | Meaning |
|---|---|
| `id` | Generated primary key |
| `name` | Customer name |
| `email` | Customer email; annotated with `@NotBlank` and `@Email` |

### `Plan`

| Column | Meaning |
|---|---|
| `id` | Generated primary key |
| `name` | Plan name |
| `price` | Plan price; annotated with `@PositiveOrZero` |
| `billing_interval` | Expected values: `MONTHLY` or `YEARLY` |
| `description` | Plan description |

### `Subscription`

| Column | Meaning |
|---|---|
| `id` | Generated primary key |
| `customer_id` | Foreign key to `Customer.id` |
| `plan_id` | Foreign key to `Plan.id` |
| `start_date` | Subscription start date |
| `next_renewal_date` | Next eligible renewal date |
| `expiry_date` | Current period expiry date |
| `status` | String status, commonly `ACTIVE` or `CANCELLED` |

### `Invoice`

| Column | Meaning |
|---|---|
| `id` | Generated primary key |
| `subscription_id` | Foreign key to `Subscription.id` |
| `amount_due` | Amount billed |
| `issue_date` | Invoice issue date |
| `due_date` | Invoice due date |
| `status` | String status, e.g. `PENDING` or `PAID` |

### `PaymentRecord`

| Column | Meaning |
|---|---|
| `id` | Generated primary key |
| `invoice_id` | Foreign key to `Invoice.id` |
| `amount_paid` | Amount recorded; annotated with `@Positive` |
| `payment_time_stamp` | Timestamp assigned by the service |
| `mode` | Payment mode label; annotated with `@NotBlank` |
| `status` | Expected values: `SUCCESS` or `FAILED` |

### Relationships

- One customer can have many subscriptions.
- One plan can be referenced by many subscriptions.
- One subscription can have many invoices.
- One invoice can have many payment records.

The relationships are represented by `@ManyToOne` fields in `Subscription`, `Invoice`, and `PaymentRecord`. Hibernate generates the schema because `spring.jpa.hibernate.ddl-auto=update` is configured. Exact physical table and column names depend on the Hibernate naming strategy and database configuration.

**Diagram files:** see `ER_Diagram.png` and `ER_Diagram.mmd` in this deliverable bundle.

## API reference

Base URL for local development: `http://localhost:8080`

All routes below are implemented in the supplied source code.

| Method | Endpoint | Purpose | Success response |
|---|---|---|---|
| `POST` | `/api/customers` | Create a customer | `201 Created` |
| `GET` | `/api/customers/{id}/subscriptions` | List subscriptions for a customer | `200 OK` |
| `GET` | `/api/plans` | List plans | `200 OK` |
| `POST` | `/api/plans` | Create a plan | `201 Created` |
| `POST` | `/api/subscriptions?customerId={id}&planId={id}` | Create a subscription and initial invoice | `201 Created` |
| `POST` | `/api/subscriptions/{id}/cancel` | Cancel a subscription | `200 OK` |
| `POST` | `/api/subscriptions/{id}/renew` | Renew an eligible subscription | `200 OK` |
| `GET` | `/api/subscriptions/{id}/invoices` | List invoices for a subscription | `200 OK` |
| `POST` | `/api/payments/invoices/{invoiceId}` | Record a simulated payment | `201 Created` |
| `GET` | `/api/payments/invoices/{invoiceId}` | List payment records for an invoice | `200 OK` |

### Request examples

#### Create a customer

`POST /api/customers`

```json
{
  "name": "Aarav Sharma",
  "email": "aarav@example.com"
}
```

#### Create a plan

`POST /api/plans`

```json
{
  "name": "Starter",
  "price": 499.0,
  "billingInterval": "MONTHLY",
  "description": "Monthly starter subscription"
}
```

`billingInterval` is expected to be `MONTHLY` or `YEARLY`.

#### List plans

`GET /api/plans`

#### Create a subscription

`POST /api/subscriptions?customerId=1&planId=1`

This is a query-parameter request, not a JSON-body request. The service looks up the customer and plan, creates an active subscription, computes the initial dates, and creates a pending invoice.

#### Cancel a subscription

`POST /api/subscriptions/1/cancel`

#### Renew a subscription

`POST /api/subscriptions/1/renew`

The current implementation rejects renewal before `nextRenewalDate` and rejects a subscription whose status is `CANCELLED`.

#### List invoices for a subscription

`GET /api/subscriptions/1/invoices`

#### Record a simulated payment

`POST /api/payments/invoices/1`

```json
{
  "amountPaid": 499.0,
  "mode": "SIMULATED",
  "status": "SUCCESS"
}
```

The payment amount must exactly equal the invoice amount due. A successful payment changes the invoice status to `PAID`. `FAILED` records a failed outcome and does not mark the invoice paid.

#### List payment records for an invoice

`GET /api/payments/invoices/1`

## Business rules currently implemented

- A plan price below zero is rejected by the service; the entity also declares `@PositiveOrZero`.
- A customer and plan must exist before a subscription can be created.
- A customer cannot create another subscription to the same plan while an `ACTIVE` subscription for that customer/plan already exists.
- Initial subscription status is `ACTIVE`.
- For `MONTHLY`, initial renewal and expiry dates are one month from today; for `YEARLY`, they are one year from today.
- Subscription creation creates an invoice with the plan price, issue date today, due date seven days later, and status `PENDING`.
- A cancelled subscription cannot be cancelled again.
- A cancelled subscription cannot be renewed.
- Renewal is rejected if today is before the `nextRenewalDate`.
- Payment processing is rejected when the invoice is already `PAID`.
- Payment amount must equal the invoice amount due exactly.
- Payment status is expected to be `SUCCESS` or `FAILED`.

## Prerequisites

- A JDK compatible with the Java version configured in `pom.xml`.
- PostgreSQL running locally.
- A PostgreSQL database named `subscription_db` (or update the configured JDBC URL).
- Maven is optional if using the included Maven Wrapper.
- An API client such as Postman or curl.

## Configuration

The supplied `src/main/resources/application.properties` configures PostgreSQL:

```properties
spring.application.name=billingsystem
spring.datasource.url=jdbc:postgresql://localhost:5432/subscription_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Set environment variables `DB_USERNAME` and `DB_PASSWORD` in your terminal or IDE run configuration before starting the application. The source ZIP contains a `.env` file; do not commit credentials or rely on Spring Boot automatically loading `.env` unless you have explicitly configured that behaviour. Prefer environment variables or a secrets manager.

Example environment-variable setup in PowerShell:

```powershell
$env:DB_USERNAME="your_postgres_username"
$env:DB_PASSWORD="your_postgres_password"
```

Create the database if needed from `psql` or pgAdmin:

```sql
CREATE DATABASE subscription_db;
```

## Run locally

From the project root (`billingsystem/`):

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS/Linux

```bash
./mvnw spring-boot:run
```

To run tests:

```bash
./mvnw test
```

The included test class currently contains a Spring application context-load test. It is not a comprehensive business-rule or endpoint test suite.

The API should be available at `http://localhost:8080` unless the server port is changed.

## Error handling

`GlobalExceptionHandler` is annotated with `@RestControllerAdvice` and currently handles:

- `ResourceNotFoundException` → `404 Not Found`
- `BusinessRuleException` → `409 Conflict`
- `MethodArgumentNotValidException` → `400 Bad Request`, with field-level errors
- `HttpMessageNotReadableException` → `400 Bad Request`
- `MethodArgumentTypeMismatchException` → `400 Bad Request`
- Other uncaught exceptions → generic `500 Internal Server Error`

A response typically contains `timestamp`, `status`, `error`, and `message`. Validation failures also include `fieldErrors`.

## Testing

Use Postman to test both successful and unsuccessful flows. Suggested test checklist:

- [ ] Create a customer with a valid email.
- [ ] Attempt to create a customer with a malformed email and confirm validation is actually triggered.
- [ ] Create monthly and yearly plans.
- [ ] Attempt to create a plan with a negative price.
- [ ] List plans.
- [ ] Create a subscription with valid customer and plan IDs.
- [ ] Attempt to subscribe with a nonexistent customer or plan.
- [ ] Attempt to create a duplicate active subscription for the same customer and plan.
- [ ] Verify the initial invoice is created and is `PENDING`.
- [ ] List customer subscriptions and subscription invoices.
- [ ] Attempt renewal before `nextRenewalDate`; expect conflict.
- [ ] Attempt to cancel an already-cancelled subscription.
- [ ] Attempt to renew a cancelled subscription.
- [ ] Record a payment with the exact amount and status `SUCCESS`; verify invoice becomes `PAID`.
- [ ] Attempt to pay an already-paid invoice.
- [ ] Attempt a mismatched payment amount.
- [ ] Record a `FAILED` simulated payment and verify invoice remains unpaid.
- [ ] Request an unknown resource and verify the error response.

**Important:** The current controllers accept entities directly and do not annotate request bodies with `@Valid`. As a result, bean validation annotations may not run on all incoming requests until `@Valid` is added. Some missing-resource paths in `CustomerService` and `SubscriptionService` use generic `RuntimeException` instead of `ResourceNotFoundException`, so they may produce `500` rather than `404`. Test the actual behaviour rather than assuming every checklist item passes.

## Known limitations and recommended improvements

These are gaps visible in the supplied code and are recommendations, not claims of implemented features:

1. **Enable validation:** add `@Valid` to request-body parameters and add required-field constraints such as `@NotBlank` to appropriate customer/plan fields.
2. **Use DTOs:** avoid binding JPA entities directly to HTTP requests/responses; define request and response DTOs.
3. **Standardise not-found exceptions:** replace generic `RuntimeException` uses with `ResourceNotFoundException`.
4. **Use precise money types:** replace `double` for prices and payment amounts with `BigDecimal`, plus explicit currency and rounding rules.
5. **Make operations transactional:** subscription creation writes a subscription and invoice; payment success updates an invoice and inserts a payment record. Use `@Transactional` so related writes remain consistent.
6. **Renewal invoice generation:** verify the full renewal service logic and ensure each successful renewal creates a new invoice for the next billing period. Renewal must be atomic with invoice creation.
7. **Date and status consistency:** update expiry dates during renewal, define whether cancellation is immediate or end-of-period, and formalise allowed statuses/transitions.
8. **Payment idempotency and reconciliation:** prevent duplicate payment submissions and model external payment references if a sandbox gateway is added.
9. **Database constraints:** consider unique email constraints, foreign-key/nullability rules, indexes, and constraints preventing duplicate active subscriptions where supported.
10. **Security:** add authentication, customer ownership checks, and role-based authorisation before exposing customer or billing data.
11. **Tests:** add unit tests for services and integration tests for endpoints and negative cases.
12. **API documentation:** add an OpenAPI/Swagger specification and a Postman collection.
13. **Observability:** add structured logs and avoid returning sensitive exception details.
14. **Secrets:** remove credentials from tracked files and rotate any credentials that were committed.

## Security and payment safety

- This is a simulated educational workflow, not a payment processor.
- Do not store card numbers, CVV, banking credentials, or other sensitive payment credentials.
- Do not connect it to real payment services without a properly designed integration, security review, and sandbox testing.
- Do not deploy publicly as-is: the API has no authentication or authorisation.

## Project structure

```text
billingsystem/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/com/project/billingsystem/
    │   │   ├── BillingsystemApplication.java
    │   │   ├── controller/
    │   │   ├── entity/
    │   │   ├── exception/
    │   │   ├── repository/
    │   │   └── service/
    │   └── resources/application.properties
    └── test/java/com/project/billingsystem/
```

## Deliverables

- `README.md` — project guide and API documentation.
- `ER_Diagram.png` — rendered entity-relationship diagram.
- `ER_Diagram.mmd` — editable Mermaid ER diagram source.
- `database_schema.sql` — illustrative relational schema matching the five entities and relationships.
- `Subscription_Billing_Management_System.pptx` — project and solution presentation.

The SQL file is a readable reference schema. Hibernate currently generates/updates the schema from JPA entities; the SQL file is not automatically executed by the application.
