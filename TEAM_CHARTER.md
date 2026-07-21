# ORCA Team Charter

This charter defines how the ORCA team collaborates, writes code, manages the repository, reviews contributions, and protects project data.

## Table of Contents

1. [Workflow](#1-workflow)
2. [Clean Code Standards](#2-clean-code-standards)
3. [Communication and SLAs](#3-communication-and-slas)
4. [Project Architecture and Repository Rules](#4-project-architecture-and-repository-rules)
5. [Continuous Improvement and Team Growth](#5-continuous-improvement-and-team-growth)
6. [Security and Data Protection](#6-security-and-data-protection)

---

## 1. Workflow

**Prepared by:** Shatha

### 1.1 Team Members

| Member Number | Name        |
|---------------|-------------|
| Member 1      | Shatha      |
| Member 2      | Raghad      |
| Member 3      | Abd Al Hade |
| Member 4      | Fatima      |
| Member 5      | Saja        |

### 1.2 Branch Strategy

Each team member works on a separate branch:

- `feature/charter-shatha`
- `feature/charter-fatima`
- `feature/charter-abd-al-hade`
- `feature/charter-saja`
- `feature/charter-raghad`

The `main` branch contains only reviewed and approved work.

### 1.3 Commit Rules

- Write clear commit messages.
- Keep each commit focused on one logical change.
- Use a suitable commit type when possible.

Examples:

```text
docs: add workflow section
docs: update clean code standards
fix: correct typo
```

### 1.4 Pull Request Workflow

1. Create a branch for the assigned task.
2. Complete and review the changes locally.
3. Commit the changes with a clear message.
4. Push the branch to GitHub.
5. Open a Pull Request.
6. Address review feedback.
7. Obtain at least two approvals before merging into `main`.

### 1.5 Merge Rules

- Do not push directly to `main`.
- Every change must go through a Pull Request.
- Merge conflicts should be resolved carefully and discussed with the team when needed.
- Pull Requests must not be merged before the required reviews and approvals.

---

## 2. Clean Code Standards

**Prepared by:** Fatima

The team follows these core rules:

1. Write readable code.
2. Use clear and meaningful names.
3. Make each function perform one task.
4. Reduce duplication by reusing functions.
5. Keep solutions simple.
6. Break large functions and classes into smaller parts.
7. Use consistent formatting and clear error handling.
8. Keep code easy to maintain.
9. Write tests for important behavior.
10. Leave the code cleaner than you found it.

### 2.1 Write Readable Code

Code should be understandable to someone reading it for the first time. Its purpose and flow should be clear without requiring unnecessary analysis.

Unclear:

```kotlin
val x = 10
val y = 5
val z = x * y
```

Clearer:

```kotlin
val itemPrice = 10
val quantity = 5
val totalPrice = itemPrice * quantity
```

### 2.2 Use Clear and Meaningful Names

Names should describe the purpose of variables, functions, and classes.

Avoid vague names such as:

- `x`
- `temp`
- `data`
- `num`
- `value`

Prefer descriptive names such as:

- `studentName`
- `totalPrice`
- `isLoggedIn`
- `calculateAverage()`

Unclear:

```kotlin
fun calc(a: Int, b: Int): Int {
    return a + b
}
```

Clearer:

```kotlin
fun calculateSum(firstNumber: Int, secondNumber: Int): Int {
    return firstNumber + secondNumber
}
```

### 2.3 Make Each Function Do One Thing

A function should have one clear responsibility. Reading data, validating it, calculating results, printing output, and saving data should not all happen inside one large function.

Prefer separating responsibilities:

```kotlin
data class Student(val grades: List<Double>)

fun validateStudent(student: Student) {
    require(student.grades.isNotEmpty()) { "Grades cannot be empty" }
}

fun calculateAverage(student: Student): Double {
    return student.grades.average()
}

fun printAverage(average: Double) {
    println(average)
}

fun saveStudent(student: Student) {
    println("Saving student with ${student.grades.size} grades")
}
```

### 2.4 Reduce Duplication

Repeated logic should be moved into a reusable function.

Instead of repeating the discount formula:

```kotlin
fun applyDiscount(price: Double): Double {
    return price - (price * 0.1)
}
```

The same function can then be reused wherever needed.

### 2.5 Keep the Code Simple

Choose the clearest solution that meets the requirement.

Avoid:

- Deeply nested conditions.
- Unnecessarily long functions.
- Very long lines.
- Clever shortcuts that reduce readability.

For example:

```kotlin
fun isEligible(
    age: Int,
    hasId: Boolean,
    isVip: Boolean,
    isBlocked: Boolean,
    points: Int
): Boolean {
    val isAdultWithId = age > 18 && hasId
    val isEligibleVip = isVip && !isBlocked && points > 100

    return isAdultWithId || isEligibleVip
}
```

This is clearer than placing every condition directly inside one complex `if`.

### 2.6 Break Large Components into Smaller Parts

Large functions and classes should be divided into small, focused components.

For example:

```kotlin
fun readStudentInput() {
    TODO("Implementation goes here")
}

fun validateStudentInput() {
    TODO("Implementation goes here")
}

fun calculateStudentResult() {
    TODO("Implementation goes here")
}

fun printStudentResult() {
    TODO("Implementation goes here")
}

fun saveStudentResult() {
    TODO("Implementation goes here")
}
```

### 2.7 Formatting and Error Handling

- Use consistent indentation and spacing.
- Follow one brace style.
- Keep related code together.
- Write clear error messages.
- Handle invalid states predictably.

Example:

```kotlin
fun divide(a: Int, b: Int): Int {
    require(b != 0) { "Divisor cannot be zero" }
    return a / b
}
```

### 2.8 Maintainability

Code should be easy to change without affecting unrelated parts.

To improve maintainability:

- Separate responsibilities.
- Use descriptive names.
- Reduce duplication.
- Keep functions small.
- Avoid unnecessary coupling between components.

### 2.9 Testing

Tests should verify important behavior and protect existing functionality from accidental changes.

Example:

```kotlin
fun add(a: Int, b: Int): Int {
    return a + b
}

fun testAdd() {
    check(add(2, 3) == 5)
}
```

### 2.10 Leave the Code Cleaner

Whenever a file is modified, improve small issues when it is safe and relevant.

Examples:

- Rename a vague variable.
- Remove duplicated or unused code.
- Simplify a long condition.
- Improve formatting.
- Split an oversized function.

Small, continuous improvements are preferred over unnecessary rewrites.

---

## 3. Communication and SLAs

**Prepared by:** Abd Al Hade

### 3.1 Communication Channels

- **WhatsApp:** Main channel for quick updates, questions, and coordination.
- **Trello:** Optional tool for task organization, ownership, and progress tracking.
- **Miro:** Optional tool for visual planning, workflows, and responsibility mapping.

### 3.2 Communication Rules

- Keep communication clear, respectful, and task-focused.
- Share progress, blockers, and questions early.
- Discuss changes that affect other members before applying them.
- Do not edit another member's assigned section without prior agreement.

### 3.3 Follow-up Meetings

During follow-ups, each member should share:

- Completed work.
- Pending work.
- Current blockers.
- Support needed from the team.

The team may agree on suitable meeting times through WhatsApp. Before final submission, the full `TEAM_CHARTER.md` must be reviewed for completeness and consistency.

### 3.4 Peer Review Rules

- Every Pull Request must be reviewed before merging.
- Each Pull Request should contain one focused task or section.
- Titles and descriptions must clearly explain the change.
- Reviewers should check correctness, clarity, formatting, naming, and scope.
- Feedback must be respectful, specific, and actionable.
- The author must address required changes before requesting final approval.
- At least two team members must approve the Pull Request.

### 3.5 Response Time and SLA Boundaries

| Item                                | Expected Response              |
|-------------------------------------|--------------------------------|
| Normal team messages                | Within 24 hours when possible  |
| Pull Request reviews                | Within 24 hours when possible  |
| Merge conflicts or blocked tasks    | As soon as possible            |
| Expected delay or absence           | Inform the team early          |

No change may be merged into `main` without the required review and approval.

---

## 4. Project Architecture and Repository Rules

**Prepared by:** Raghad

### 4.1 Architecture Overview

The project follows a modular architecture. Each component has a clear responsibility and communicates with other components through defined boundaries.

Main goals:

- Maintain a clean and organized structure.
- Reduce unnecessary dependencies.
- Support future growth.
- Make testing and debugging easier.

### 4.2 Directory Structure

src/
├── main/
│───kotlin/
│    ├── algorithms/
│    ├── model/
│    ├── parser/
|    ├── Main.kt
│─── resources/
└── test/
    ├── unit/
    └── integration/


### 4.3 Directory Responsibilities

- **Controller:** Handles requests and inputs, then communicates with the service layer.
- **Service:** Contains business logic and application rules.
- **Repository:** Manages data access and external storage.
- **Model:** Contains data objects and entities.
- **Utils:** Contains reusable helper functions.
- **Config:** Contains application configuration.
- **Test:** Contains unit and integration tests.


### 4.4 Use Case Organization

Each use case should represent one user goal or business operation.

Example:

usecases/
├── authentication/
│   ├── Login
│   └── Register
├── userManagement/
│   ├── CreateUser
│   └── UpdateUser
└── reporting/
    ├── GenerateReport
    └── ExportData

Benefits:

- Clearer feature ownership.
- Better separation of business logic.
- Easier testing.
- Safer future changes.

### 4.5 Repository Management Rules

- Commit only required source and configuration files.
- Do not push temporary, generated, or machine-specific files.
- Never store passwords, API keys, tokens, or private credentials in the repository.
- Follow the agreed Git workflow.
- Introduce all changes through Pull Requests.

### 4.6 `.gitignore` Configuration

The `.gitignore` file keeps unnecessary and sensitive files out of the repository.

Recommended entries:

```gitignore
# IDE files
.idea/
.vscode/
*.iml

# Build output
build/
target/
out/

# Compiled files
*.class

# Logs
*.log

# Environment files
.env

# Operating system files
.DS_Store
Thumbs.db
```

> Note: Required project dependencies and build files should remain tracked when the project needs them.

### 4.7 Architecture Maintenance

Before making a major structural change:

1. Discuss it with the team.
2. Evaluate its effect on existing components.
3. Update documentation when needed.
4. Confirm that the project still builds successfully.
5. Run relevant tests.

---

## 5. Continuous Improvement and Team Growth

**Prepared by:** Raghad

> A team that learns and grows together.

### 5.1 Shared Learning

- Knowledge belongs to the team, not one individual.
- Members should share useful discoveries through short walkthroughs, notes, or discussions.
- Questions are welcome, regardless of experience level.
- Explaining a concept helps both the learner and the person explaining it.

### 5.2 Feedback

Feedback is a tool for improvement, not personal criticism.

During reviews:

- Focus on the work, not the person.
- Explain the reason behind suggestions.
- Keep comments respectful and actionable.
- Use each review as an opportunity to align on quality standards.

After important deliveries, the team should reflect on:

- What went well?
- What caused delays?
- What should change next time?
- What actions should be carried into the next phase?

### 5.3 Innovation

Continuous learning and research are part of the team's responsibility.

The team should:

- Question assumptions when needed.
- Consider cleaner solutions.
- Evaluate tools that may save time or improve quality.
- Welcome useful suggestions from every member.
- Avoid changing tools or approaches without discussing their impact.

The team values honesty, respectful feedback, shared responsibility, and continuous growth.

---

## 6. Security and Data Protection

**Prepared by:** Saja

### 6.1 Secure Coding Practices

- Never hard-code passwords, API keys, or sensitive information.
- Validate user input before processing it.
- Do not expose sensitive details in logs or error messages.
- Keep dependencies updated when appropriate.
- Follow the principle of least privilege.

### 6.2 Data Protection Rules

- Collect sensitive data only when necessary.
- Limit data access to authorized components.
- Do not share personal information outside the project environment.
- Consider backup and recovery for important data.

### 6.3 Security Review

Before merging major changes, reviewers should check for:

- Exposed credentials or sensitive files.
- Missing input validation.
- Weak authentication or authorization logic.
- Unnecessary access or permissions.
- New risks introduced by the change.

Security concerns must be reported to the team immediately.

### 6.4 Shared Responsibility

Every team member should:

- Follow the agreed security guidelines.
- Report discovered risks.
- Learn and apply secure development practices.
- Consider security during design, implementation, and review.
