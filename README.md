#  LogiRoute Hub

**LogiRoute Hub** is a Kotlin-based logistics management system that models and manages a distributed delivery network consisting of warehouses, packages, vehicles, routes, and delivery operations.

The project began as a domain-modeling exercise and gradually evolved into a modular software system combining:

* Object-Oriented Design
* SOLID Principles
* Clean Architecture
* Repository Abstraction
* Domain Validation
* Functional Error Handling
* Design Patterns
* Graph Algorithms
* Tree Data Structures
* Greedy Optimization
* Functional Collection Pipelines
* Local and Remote Data Sources

The project was developed incrementally, with each stage introducing new architectural or algorithmic requirements and refactoring the existing system accordingly.

> **Model the network. Optimize the routes. Handle failures explicitly. Deliver smarter. 🚚**

---

#  Project Journey

The project evolved through several stages, starting from a simple logistics domain and gradually becoming a more structured application with separated domain, data, validation, algorithmic, and infrastructure responsibilities.

---

## 1.  Domain Modeling

The foundation of LogiRoute Hub is a domain model representing the core elements of a logistics network.

The main domain entities are:

###  Warehouse

Represents a logistics hub responsible for managing:

* Packages
* Cargo queues
* Routes
* Stationed vehicles
* Warehouse relationships

###  Package

Represents a logistics package with properties such as:

* Weight
* Priority
* Origin warehouse
* Destination warehouse
* Tracking information

###  Vehicle

Represents a delivery vehicle with:

* Maximum capacity
* Operating information
* Current warehouse
* Loaded packages

###  Route

Represents a connection between warehouses with properties such as:

* Distance
* Expected delay
* Source warehouse
* Destination warehouse

The domain model is kept independent from the concrete data source so that business logic does not depend directly on CSV files, Supabase, or other infrastructure details.

---

# 2.  Data Layer Evolution

The data layer evolved from simple CSV-based input into a more structured data-access architecture.

The current project separates data access into several responsibilities:

```text
Data Source
     ↓
Parser / Remote Data Source
     ↓
Mapper
     ↓
Repository Implementation
     ↓
Repository Interface
     ↓
Domain / Use Cases
```

The project currently supports both:

### Local Data

CSV-based data sources are used for local repository implementations:

```text
CSV
 ↓
Parser
 ↓
Data Source
 ↓
Mapper
 ↓
Local Repository
```

### Remote Data

The system also integrates with remote data sources using **Supabase/PostgREST**:

```text
Use Case
   ↓
Repository Interface
   ↓
Remote Repository
   ↓
Remote Data Source
   ↓
Supabase / PostgREST
```

This separation allows the domain layer to remain independent from the concrete storage mechanism.

---

# 3.  Repository Abstraction

Repository interfaces are defined inside the domain layer:

```kotlin
WarehouseRepository
PackageRepository
RouteRepository
VehicleRepository
```

Concrete implementations exist separately in the data layer.

This follows the **Dependency Inversion Principle**:

```text
                 Domain
                   │
                   ▼
          Repository Interface
                   ▲
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
 Local Repository       Remote 
Repository
        │                     │
        ▼                     ▼
       CSV                 Supabase
```

The same domain/application logic can therefore work with different data sources without changing the repository contract.

---

# 4.  Clean Use Case Architecture

As the application grew, business operations were extracted into independent Use Cases.

Each use case focuses on a specific responsibility and commonly exposes:

```kotlin
operator fun invoke(...)
```

This supports the **Single Responsibility Principle** and keeps business operations isolated and testable.

The current project contains use cases covering areas such as:

### Routing

* `FindOptimalPathUseCase`
* `FindFewestHopsRouteUseCase`
* `ReroutePackageUseCase`
* `CheckWarehouseConnectivityUseCase`

### Package & Cargo Operations

* `AssignPackageToCargoQueueUseCase`
* `SortingCargoQueueUseCase`
* `PrioritizeDispatchQueueUseCase`
* `GetPackageJourneyUseCase`
* `AnalyzePackageDestinationDemandUseCase`

### Vehicle & Fleet Operations

* `AddVehicleToHubUseCase`
* `DispatchVehicleUseCase`
* `FindStationedVehiclesByCapacityUseCase`
* `RecommendVehicleByCapacityAndCostUseCase`
* `ValidateVehicleCapacityUseCase`
* `TriggerFleetRebalancingUseCase`

### Analysis

* `GetWarehouseLoadFactorUseCase`
* `DetectPotentialDeliveryBottleneckUseCase`
* `EstimateDeliveryTimeUseCase`
* `EstimateRouteRiskUseCase`
* `AnalyzeTreePerformanceUseCase`

### Pricing

* `CalculatePricingUseCase`

### CRUD

Dedicated CRUD use cases are provided for:

* Warehouses
* Packages
* Routes
* Vehicles

including:

```text
Create
Read
Update
Delete
```

This allows application operations to remain separate from the underlying repository implementations.

---

# 5.  Functional Collection Pipelines

The project applies Kotlin's functional collection operations in several business operations.

Instead of relying exclusively on imperative loops, collection processing makes use of operations such as:

```text
map
filter
fold
reduce
flatMap
associateBy
maxByOrNull
firstOrNull
```

For example, package dispatch capacity can be calculated using a functional pipeline:

```text
Cargo Queue
     ↓
fold(...)
     ↓
Selected Packages + Remaining Capacity
```

The project also uses collection pipelines when preparing routing, dispatching, warehouse indexing, and analysis data.

This supports a more declarative style of data processing.

---

# 6.  Domain Validation Architecture

A dedicated validation layer was introduced to separate domain validation from business operations.

The validation architecture contains:

```text
Validator
    │
    ├── Validation Rules
    │
    └── ValidationResult
            │
       ┌────┴────┐
       ▼         ▼
    Success   Failure
```

The project contains reusable validation rules such as:

* `IdValidator`
* `NonBlankStringValidator`
* `LatitudeValidator`
* `LongitudeValidator`
* `PositiveNumberValidator`
* `NonNegativeNumberValidator`
* `PriorityValidator`
* `DifferentLocationsValidator`

Entity-specific validators are also provided for create/update operations of:

* Warehouses
* Packages
* Routes
* Vehicles

This keeps validation rules centralized and reusable.

---

# 7.  Functional Error Handling — Strategy A

One of the most important architectural requirements introduced during the project's evolution was the adoption of:

# **Strategy A — Functional Error Handling**

The project uses **Kotlin's ****`Result<T>`** as the primary mechanism for representing success and failure across the repository and CRUD application flow.

Instead of exposing infrastructure exceptions directly to higher layers, repository operations return:

```kotlin
Result<T>
```

For example:

```kotlin
interface WarehouseRepository {

    suspend fun findById(
        warehouseId: String
    ): Result<Warehouse?>

    suspend fun findAll(): Result<List<Warehouse>>

    suspend fun add(
        warehouse: Warehouse
    ): Result<Warehouse>

    suspend fun update(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Result<Warehouse>

    suspend fun delete(
        warehouseId: String
    ): Result<Unit>
}
```

This makes success and failure explicit in the API contract.

---

##  Result-Based Error Flow

The current architecture follows this general flow:

```text
Remote / Local Data Source
          │
          ▼
Repository Implementation
          │
       runCatching
          │
          ▼
       Result<T>
       /       \
   Success    Failure
                 │
                 ▼
          Domain Exception
```

A successful operation produces:

```kotlin
Result.success(value)
```

while failures are represented through:

```kotlin
Result.failure(exception)
```

---

# 8.  Infrastructure Error Mapping

The project separates infrastructure errors from domain-level errors.

Remote errors are first captured by the data layer and then mapped into typed domain exceptions.

The project contains a dedicated:

```text
DataExceptionMapper
```

which translates infrastructure failures such as:

* Network failures
* HTTP errors
* Timeouts
* Supabase/PostgREST errors
* Not-found responses
* Database conflicts

into domain-level exceptions.

The flow is:

```text
Supabase / Network Error
          ↓
Infrastructure Exception
          ↓
DataExceptionMapper
          ↓
Domain Exception
          ↓
Result.failure(...)
```

This prevents infrastructure-specific exception types from leaking into the domain layer.

---

# 9.  Typed Domain Exception Hierarchy

The domain layer defines typed exceptions such as:

```text
DomainException
├── NetworkUnavailableException
├── EntityValidationException
├── ResourceNotFoundException
└── EntityConflictException
```

This allows failures to retain meaningful business-level information.

For example:

### NetworkUnavailableException

Represents network connectivity or timeout failures.

### ResourceNotFoundException

Represents attempts to access a resource that does not exist.

### DatabaseConflictException

Represents conflicts such as duplicate or conflicting records.

### EntityValidationException

Represents invalid domain input detected by the validation layer.

---

# 10. 🔗 Result + Validation Integration

The Functional Error Strategy is also integrated with CRUD use cases.

For example, the create warehouse flow is:

```text
CreateWarehouseUseCase
        │
        ▼
CreateWarehouseValidator
        │
   ┌────┴─────┐
   ▼          ▼
Success     Failure
   │          │
   ▼          ▼
Repository  Result.failure(
   │        EntityValidationException
   ▼          )
Result<Warehouse>
```

Therefore, validation failures and repository failures can both be represented through the same `Result<T>` contract.

This provides a consistent failure-propagation mechanism at the application boundary.

---

# 11.  Strategy Pattern — Dynamic Pricing

The pricing subsystem uses the **Strategy Pattern**.

A common abstraction is defined by:

```kotlin
interface DispatchStrategy
```

Concrete strategies include:

* `ExpressStrategy`
* `EcoStrategy`
* `FragileStrategy`

The `RoutePricingEngine` delegates pricing behavior to the selected strategy.

```text
                 DispatchStrategy
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
      Express          Eco         Fragile
      Strategy       Strategy      Strategy
```

This makes the pricing system extensible without modifying existing pricing logic.

---

# 12.  Decorator Pattern — Package Services

The project uses the **Decorator Pattern** to dynamically add additional services to packages.

Implemented decorators include:

* `FragileHandlingDecorator`
* `ColdChainDecorator`
* `ExpressInsuranceDecorator`

The decorators can be composed through:

```text
PackageDecoratorComposer
```

For example:

```text
Package
   ↓
Fragile Handling
   ↓
Cold Chain
   ↓
Express Insurance
```

This allows additional package behavior to be composed without modifying the original package model.

---

# 13.  Graph Routing Algorithms

The logistics network is represented as a graph:

```text
Warehouse = Node
Route     = Edge
```

The project implements multiple routing algorithms.

## Dijkstra's Algorithm

`ShortestPathRouter` and `FindOptimalPathUseCase` use weighted shortest-path logic.

Dijkstra's algorithm is appropriate when route distance matters.

```text
A ── 10 km ── B ── 20 km ── D
│
└──── 50 km ─────────────── D
```

The algorithm determines the path with the minimum accumulated distance.

---

## Breadth-First Search

`LeastHopRouter` is used to find paths based on the minimum number of warehouse-to-warehouse hops.

This is different from shortest-distance routing because the objective is:

```text
Minimum number of edges
```

rather than minimum distance.

---

## Bidirectional Search

The project also implements:

```text
BidirectionalRouter
```

which searches from both the source and destination to reduce the explored search space in suitable unweighted routing scenarios.

Dedicated validation classes are used to verify routing behavior.

---

# 14.  Manual Sorting Algorithms

The project deliberately implements sorting algorithms manually for selected requirements.

The system includes package sorting functionality such as:

```text
SortingCargoQueueUseCase
AssignPackageToCargoQueueUseCase
PackageSelectionSorter
```

The cargo queue can be organized according to package characteristics such as weight or priority.

This keeps algorithmic behavior explicit instead of hiding it entirely behind library sorting functions.

---

# 15.  Hierarchical Warehouse Trees

Warehouses are also represented through a hierarchical tree structure.

The hierarchy uses warehouse levels such as:

```text
GLOBAL
   │
   ├── REGIONAL
   │      ├── LOCAL
   │      └── LOCAL
   │
   └── REGIONAL
          ├── LOCAL
          └── LOCAL
```

The project contains:

* `WarehouseLevel`
* `HubTreeWarehouseNode`
* `HubTree`
* `HubTreeBuilder`
* `HubLineageTracer`

This allows the system to construct warehouse hierarchies and trace a local warehouse back through its parent chain.

---

# 16. 📈 Balanced vs. Unbalanced BST Simulation

The project includes a dedicated tree-performance simulation.

A sequence of tracking IDs can be generated using:

```text
SequentialTrackingIdGenerator
```

The system then compares:

```text
UnbalancedBST
vs.
BalancedBST
```

using generated package tracking IDs.

The analysis records metrics such as:

* Search steps
* Execution time
* Tree type

This provides a practical demonstration of how tree balance affects search behavior and performance.

---

# 17.  Greedy Fleet Dispatch

The project also implements a **Greedy Set-Covering approach** for fleet dispatch.

The goal is to select vehicles that collectively cover a set of target zones.

At each iteration, the algorithm selects the available vehicle that covers the largest number of currently uncovered zones.

```text
Target Zones
      │
      ▼
Available Vehicle Coverage
      │
      ▼
Select Best Current Coverage
      │
      ▼
Remove Covered Zones
      │
      ▼
Repeat
```

The project separates the preparation of greedy input from the dispatch algorithm through:

* `GetGreedyDispatchDataUseCase`
* `VehicleCoverageMapper`
* `GreedyFleetDispatcher`

This keeps data preparation and optimization logic independently testable.

---

# 18.  Command Pattern

The project also introduces the **Command Pattern** for dispatch-related operations.

Commands include:

* `AssignPackageToQueueCommand`
* `DispatchVehicleCommand`
* `ReroutePackageCommand`

A central:

```text
CommandInvoker
```

manages command execution.

The command system also supports:

* Undo
* Redo
* Command history
* Telemetry

The flow is:

```text
Command
   ↓
CommandInvoker
   ↓
Execute
   ↓
Undo Stack
   ↓
Redo Stack
```

Telemetry is provided through abstractions such as:

```text
CommandTelemetry
ConsoleTelemetry
```

This keeps command execution history separate from the commands themselves.

---

# 19.  Validation & Algorithm Verification

The project includes dedicated validation suites for major components, including:

* Dijkstra routing
* Least-hop routing
* Bidirectional routing
* Pricing strategies
* Decorator integration
* Hub tree construction
* Command pattern
* Command history

These validation classes provide scenario-based checks for the implemented algorithms and design patterns.

---

#  Current Architecture

The current system can be viewed as a layered architecture:

```text
                    ┌──────────────────────┐
                    │     Presentation     │
                    │        Main          │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │      Use Cases       │
                    │                      │
                    │ Business Operations  │
                    └──────────┬───────────┘
                               │
              ┌────────────────┼────────────────┐
              │                                 │
              ▼                                 ▼
       ┌────────────┐                   ┌──────────────┐
       │   Domain   │                   │  Algorithms  │
       │   Model    │                   │ & Structures │
       └─────┬──────┘                   └──────────────┘
             │
             ▼
       ┌────────────────────┐
       │ Repository         │
       │ Interfaces         │
       └─────────┬──────────┘
                 │
          ┌──────┴──────┐
          ▼             ▼
 ┌────────────────┐ ┌────────────────┐
 │ Local Repos    │ │ Remote Repos   │
 │                │ │                │
 │ CSV Data       │ │ Supabase       │
 └───────┬────────┘ └───────┬────────┘
         │                  │
         ▼                  ▼
    Local Data        Remote Data
```

---

#  Functional Error Flow

The most important cross-layer error flow is:

```text
                Data Source
                    │
                    ▼
             Repository Layer
                    │
               runCatching
                    │
                    ▼
                Result<T>
                 /     \
                /       \
          Success       Failure
             │             │
             │             ▼
             │      Error Mapping
             │             │
             │             ▼
             │      Domain Exception
             │             │
             └─────────────┴──────► Result<T>
```

This is the project's implementation of:

> **Strategy A — Functional Error Handling**

The key architectural idea is that failures are represented explicitly as values through `Result<T>` while infrastructure-specific errors are translated into meaningful domain exceptions at the data boundary.

---

#  Design Patterns Used

| Pattern                | Where / Purpose                                  |
| ---------------------- | ------------------------------------------------ |
| **Strategy Pattern**   | Dynamic pricing strategies                       |
| **Decorator Pattern**  | Composable package services                      |
| **Repository Pattern** | Abstract local and remote data access            |
| **Builder Pattern**    | Construction of domain/tree structures           |
| **Command Pattern**    | Dispatch actions, undo/redo, and command history |

These patterns were introduced to address specific architectural requirements rather than being used simply for demonstration.

---

#  Algorithms & Data Structures

The current project includes:

### Graph Algorithms

* Dijkstra's Algorithm
* Breadth-First Search
* Bidirectional BFS
* Graph traversal

### Sorting

* Manual package sorting
* Selection-based sorting

### Tree Structures

* Hierarchical warehouse trees
* Binary Search Tree
* Balanced BST
* Tree lineage tracing

### Optimization

* Greedy Set-Covering Fleet Dispatch

### Performance Analysis

* Search-step measurement
* Execution-time measurement
* Balanced vs. unbalanced tree comparison

---

# 🗂️ Project Structure

The current backend/domain structure is organized around clear responsibilities:

```text
src/
└── main/
    └── kotlin/
        ├── data/
        │   ├── dataholder/
        │   ├── datasource/
        │   ├── exception/
        │   ├── mapper/
        │   ├── parser/
        │   ├── remote/
        │   └── repository/
        │
        ├── domain/
        │   ├── algorithm/
        │   ├── command/
        │   ├── dispatcher/
        │   ├── exception/
        │   ├── model/
        │   ├── pricing/
        │   ├── repository/
        │   ├── routing/
        │   ├── tree/
        │   ├── usecase/
        │   ├── validation/
        │   └── validator/
        │
        └── presentation/
            └── Main.kt
```

The repository also contains a frontend module built around Kotlin Multiplatform/Android Compose infrastructure.

---

#  Technologies

* **Kotlin 2.1.10**
* **JVM 17**
* **Gradle**
* **Supabase / PostgREST**
* **Ktor**
* **CSV Data Processing**
* **Kotlin Serialization**
* **Object-Oriented Programming**
* **SOLID Principles**
* **Clean Architecture**
* **Functional Programming**
* **Kotlin ****`Result<T>`**
* **Design Patterns**
* **Graph Algorithms**
* **Tree Data Structures**
* **Git & GitHub**
* **Detekt**
* **Android / Compose Multiplatform**

---

#  Engineering Principles

The project applies several software engineering principles throughout its evolution.

### Single Responsibility

Business operations are separated into independent Use Cases.

### Dependency Inversion

Domain logic depends on repository interfaces rather than concrete data sources.

### Separation of Concerns

Data access, domain logic, validation, algorithms, and infrastructure concerns are separated into dedicated packages.

### Open/Closed Principle

Strategy and Decorator patterns allow new behaviors to be introduced without modifying existing implementations.

### Explicit Error Handling

Repository and CRUD operations use `Result<T>` to make success and failure explicit.

### Infrastructure Isolation

Remote infrastructure errors are mapped into domain exceptions before reaching higher layers.

### Algorithmic Transparency

Important algorithms are implemented explicitly to demonstrate their behavior, complexity, and trade-offs.

---

# Built With Kotlin

**LogiRoute Hub**

> **Model the network. Optimize the routes. Handle failures explicitly. Deliver smarter. **
