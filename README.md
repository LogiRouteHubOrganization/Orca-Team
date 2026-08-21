# 🚚 LogiRoute Hub

**LogiRoute Hub** is a Kotlin-based logistics management system designed to model and manage a distributed delivery network consisting of warehouses, packages, vehicles, and transportation routes.

The project was developed incrementally, starting from a clean domain model and gradually evolving into a modular system that applies **Object-Oriented Design, SOLID principles, Design Patterns, abstraction, manual algorithms, and clean architecture practices**.

Rather than relying on built-in shortcuts, several core operations are implemented manually to strengthen algorithmic understanding and maintain full control over the system's behavior.

---

## Project Journey

The project started with a simple question:

> **How can we model a real logistics network in a way that remains flexible, maintainable, and easy to extend?**

From that starting point, LogiRoute Hub evolved through several stages.

### 1.  Domain Modeling

The first step was defining the core entities of the logistics system.

The domain model consists of:

* **Warehouse** — represents a logistics hub and manages packages, routes, and stationed vehicles.
* **LogisticsPackage** — represents a package with weight, priority, origin, and destination.
* **Route** — represents a connection between two warehouses with a defined distance and expected delay.
* **Vehicle** — represents a delivery vehicle with capacity, operating cost, and current location.

The goal of this stage was to create a clear and independent domain model that represents the real-world logistics network.

---

### 2.  Data Layer & Parsing

After establishing the domain model, the project introduced a data layer responsible for handling raw input data.

CSV-based repositories and parsers were introduced to separate:

**Raw Data → Parsing → Repository → Domain**

This separation prevents the domain layer from depending directly on CSV files or raw input structures.

The architecture was later improved by introducing repository interfaces, allowing the domain-building logic to depend on **abstractions rather than concrete data sources**.

This makes it possible to replace the underlying data source in the future without rewriting the domain logic.

---

### 3. 🗺️ Domain Graph Construction

The next stage was transforming the imported logistics data into a connected domain graph.

Warehouses become nodes, while routes represent connections between them.

The `DomainGraphBuilder` is responsible for constructing this graph from repository-provided data while keeping the domain layer independent from the original CSV representation.

The resulting structure allows the system to reason about:

* Warehouse connectivity
* Package origins and destinations
* Available transportation routes
* Vehicle locations
* Package movement through the network


### 5.  Polymorphic Pricing System

As the project grew, different delivery strategies required different pricing rules.

Instead of implementing pricing logic through large conditional statements, the project introduced the **Strategy Pattern**.

A common `DispatchStrategy` interface defines the pricing behavior, while different strategies implement their own rules:

*  **Express Strategy**
*  **Eco Strategy**
*  **Fragile Strategy**

A `RoutePricingEngine` manages these strategies and delegates pricing calculations to the appropriate implementation.

This approach makes the pricing subsystem easier to extend without modifying existing logic.

---

### 6. Dynamic Package Services — Decorator Pattern

The project was further extended to support additional services that can dynamically affect package transportation.

The **Decorator Pattern** was introduced to allow packages to be wrapped with additional behavior at runtime.

Examples include:

* 🛡️ `FragileHandlingDecorator`
* ❄️ `ColdChainDecorator`
* 🚀 `ExpressInsuranceDecorator`

Multiple decorators can be combined dynamically:

```text
Package
   │
   ▼
Fragile Handling
   │
   ▼
Cold Chain
   │
   ▼
Express Insurance
```

This allows a package to acquire multiple services without modifying the original package class.

---

### 7. 📊 Manual Package Sorting

To strengthen algorithmic understanding, the project avoids relying on built-in sorting utilities for specific requirements.

A manual **in-place QuickSort** algorithm was implemented for warehouse cargo queues.

Packages are sorted according to their weight in descending order:

```text
Heavy Package
      ↓
Medium Package
      ↓
Light Package
```

The implementation focuses on understanding partitioning, recursion, and in-place sorting rather than simply calling a library function.

---

### 8. 🧭 Optimal Route Finding — Dijkstra's Algorithm

The project also introduced graph-based route optimization.

A manual implementation of **Dijkstra's Algorithm** was developed to find the shortest path between warehouses based on route distance.

This was particularly important because the logistics network contains routes with different weights.

For example:

```text
A ── 10 km ── B ── 20 km ── D
│
└──── 50 km ─────────────── D
```

Although both paths connect the same locations, their costs are different.

This demonstrates why a simple BFS approach is insufficient for weighted graphs and why Dijkstra's algorithm is more appropriate for shortest-path calculations in this system.

---

## 🏗️ Architecture

The project gradually evolved toward a layered and modular architecture:

```text
                    ┌─────────────────┐
                    │      Main       │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Domain Builder  │
                    └────────┬────────┘
                             │
                             │
                    ┌────────▼────────┐
                    │  CSV Repository │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   CSV / Raw     │
                    │      Data       │
                    └─────────────────┘
```

The domain layer remains independent from the concrete data source by relying on repository abstractions.

This provides better:

* **Separation of Concerns**
* **Maintainability**
* **Testability**
* **Extensibility**
* **Dependency Management**

---

## 🧩 Design Patterns Used

The project applies several design patterns to solve different architectural problems:
-----------------------------------------------------------------------------
| Pattern                | Purpose                                          |
| ---------------------- | ------------------------------------------------ |
| **Strategy Pattern**   | Support multiple dispatch and pricing strategies |
| **Decorator Pattern**  | Dynamically add package services                 |
| **Repository Pattern** | Abstract data access from the domain             |
| **Builder Pattern**    | Construct the domain graph from repository data  |

These patterns were not introduced only for the sake of using patterns; each one addresses a specific design problem encountered during the project's evolution.

---

## 🧠 Algorithms Implemented

The project also focuses on implementing important algorithms manually:

* 🔹 **QuickSort** — sorting warehouse cargo queues by package weight.
* 🔹 **Dijkstra's Algorithm** — finding shortest paths in the weighted logistics graph.
* 🔹 **Tree Traversal** — tracing warehouse parent lineage.
* 🔹 **Graph Traversal** — navigating the logistics network.

The implementation of these algorithms without relying on high-level built-in utilities provides a deeper understanding of their internal behavior and complexity.

## 🛠️ Technologies

* **Kotlin**
* **Gradle**
* **JVM**
* **CSV Data Processing**
* **Object-Oriented Programming**
* **SOLID Principles**
* **Design Patterns**
* **Graph Algorithms**
* **Git & GitHub**

---

## 📁 Project Structure

```text
src/
└── main/
    └── kotlin/
        ├── dataHolder/
        ├── parser/
        ├── repository/
        ├── domain/
        │   └── model/
        ├── builder/
        ├── pricing/
        ├── decorator/
        ├── algorithm/
        └── Main.kt
```

The project structure is organized around clear responsibilities, keeping data handling, domain logic, business strategies, decorators, and algorithms separated.


## 🚀 Final Vision

The ultimate goal of **LogiRoute Hub** is to provide a flexible foundation for a logistics network where packages, warehouses, vehicles, routes, pricing strategies, and delivery services can evolve independently.

What began as a domain-modeling exercise gradually became a practical exploration of:

> **Clean Architecture + Object-Oriented Design + Design Patterns + Algorithms**

The project continues to evolve as new requirements introduce new architectural and algorithmic challenges.

---

### 👩‍💻 Built With Kotlin

**LogiRoute Hub** — *Model the network. Optimize the routes. Deliver smarter.* 🚚
