# 🏪 StoreMate REST API

**StoreMate REST API** is a Kotlin-based **Spring Boot** application designed to manage stores, products, suppliers, transactions, and users.  
It provides a secure and modular REST backend with **JWT authentication** and **Spring Security**.

---

## ⚙️ Tech Stack

- **Language:** Kotlin (JVM 17)
- **Framework:** Spring Boot 3.5.x
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** H2 (in-memory, for development)
- **Security:** Spring Security + JWT
- **Persistence:** Spring Data JPA (Hibernate)
- **Serialization:** Jackson Kotlin module
- **Testing:** JUnit 5, Spring Boot Test

---

## 🧩 Architecture Overview

The project follows a **classic layered architecture**:
- **Controller layer** – exposes REST endpoints (`/api/products`, `/api/suppliers`, `/api/transactions`, `/api/auth/**`)
- **Service layer** – implements business logic (token validation, data operations)
- **Repository layer** – performs persistence using Spring Data JPA
- **Model layer** – defines JPA entities (`Product`, `Supplier`, `Transaction`, `Store`, `User`)
- **Security layer** – handles JWT authentication via a `JwtAuthenticationFilter`

Each entity belongs to a specific store and is connected through relationships:
- A `Product` belongs to a `Supplier` and a `Store`
- A `Transaction` references a `Product` and a `Store`
- A `User` is tied to one `Store`

---

## 🔐 Authentication

The API uses **JWT (JSON Web Tokens)** for authentication and authorization.  
Endpoints under `/api/auth/**` are public for login and registration.  
All other routes require a valid JWT in the `Authorization` header.

Example:
Authorization: Bearer <your_token_here>
## 🧾 Example Endpoints

### Products
| Method | Endpoint | Description |
|---------|-----------|-------------|
| `GET` | `/api/products` | Get all products (paginated) |
| `GET` | `/api/products/{id}` | Get product by ID |
| `POST` | `/api/products` | Add a new product |
| `DELETE` | `/api/products/{id}` | Delete a product by ID |

Example `ProductDTO` body:
```json
{
  "name": "Orange Juice",
  "description": "Freshly squeezed 1L bottle",
  "price": 5.99,
  "category": "Beverages",
  "barcode": "1234567890123",
  "supplierId": 2,
  "currentStockLevel": 50,
  "minimumStockLevel": 10
}
```
## 🛒 Domain Model Summary

| Entity | Description |
|---------|-------------|
| **Product** | Represents a product with name, price, stock levels, and supplier. |
| **Supplier** | Provides contact and store information for product suppliers. |
| **Transaction** | Tracks product restocks and sales. |
| **Store** | Represents a physical or virtual store. |
| **User** | Authenticated user, linked to a single store. |

DTOs (`ProductDTO`, `SupplierDTO`, `TransactionDTO`) are used for API input/output.

---

## 🚧 Next Steps

The project will be refactored into a **modern architecture** using **Clean Architecture** or **Hexagonal Architecture** principles to separate:

- `domain`, `application`, and `infrastructure` layers  
- use cases and data models independent from Spring framework  

### ✨ Benefits
- Better modularity  
- Improved testability  
- Easier scaling and maintenance  
