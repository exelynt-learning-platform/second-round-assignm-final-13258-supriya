# E-Commerce Backend System (Spring Boot)

## Overview

This project is a robust and scalable backend system for an e-commerce platform built using **Java and Spring Boot**. It provides secure user authentication, product management, cart operations, order processing, and payment integration using **Stripe**.

The system follows RESTful design principles and ensures clean architecture, maintainability, and data integrity.

---

## Features

### Authentication & Authorization

* User registration and login using **JWT authentication**
* Secure password storage using **BCrypt hashing**
* Role-based access control:

  * `USER`: Manage cart and orders
  * `ADMIN`: Manage products

### Product Management

* Create, update, delete products (Admin only)
* Fetch all products / product by ID
* Product attributes:

  * Name, Description, Price, Stock Quantity, Image URL

### Cart Management

* Each user has a personal cart
* Add, update, and remove items
* Stock validation before adding items
* Users can only access their own cart

### Order Management

* Create orders from cart
* View order history and details
* Order includes:

  * Products, total price, shipping details, status

### Payment Integration (Stripe)

* Integrated **Stripe payment gateway (test mode)**
* Handles:

  * Payment Success → Order marked as `PAID`
  * Payment Failure → Order marked as `FAILED`

---

## Tech Stack

| Layer      | Technology                  |
| ---------- | --------------------------- |
| Language   | Java                        |
| Framework  | Spring Boot                 |
| Security   | Spring Security + JWT       |
| ORM        | Spring Data JPA (Hibernate) |
| Database   | MySQL          |
| Build Tool | Gradle              |
| Payment    | Stripe API                  |
| Testing    | JUnit, Mockito              |

---

## System Architecture

* Layered Architecture:

  * Controller → Service → Repository → Database
* Follows **SOLID principles**
* Uses DTOs for request/response abstraction
* Global exception handling implemented

---

## Database Design

### Relationships:

* User → Orders (**One-to-Many**)
* Order → Products (**Many-to-Many via OrderItems**)
* User → Cart (**One-to-One**)
* Cart → Products (**via CartItems**)

---

## API Endpoints (Sample)

### Auth

```
POST /api/auth/register
POST /api/auth/login
```

### Products

```
GET /api/products
GET /api/products/{id}
POST /api/products        (ADMIN)
PUT /api/products/{id}    (ADMIN)
DELETE /api/products/{id} (ADMIN)
```

### Cart

```
GET /api/cart
POST /api/cart/add
PUT /api/cart/update
DELETE /api/cart/remove/{productId}
```

### Orders

```
POST /api/orders
GET /api/orders
GET /api/orders/{id}
```

### Payment

```
POST /api/payment/checkout
```

---

## Setup & Installation

### Prerequisites

* Java 17+
* Gradle
* MySQL
* Stripe Account (Test Mode)

---

## Security

* JWT-based stateless authentication
* Password hashing using BCrypt
* Role-based authorization
* Protected endpoints

---

## Error Handling

* Global exception handler using `@ControllerAdvice`
* Meaningful error responses:

  * 400 Bad Request
  * 401 Unauthorized
  * 404 Not Found
  * 500 Internal Server Error

---

## Testing

* Unit tests implemented for:

  * Authentication
  * Cart services
  * Order processing
* Tools used:

  * JUnit
  * Mockito

---


---

