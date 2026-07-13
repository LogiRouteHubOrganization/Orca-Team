# ORCA TEAM CHARTER

## Member 1 - Workflow

### Team Members
- Shatha
- Fatima
- Abd alHadi
- Saja
- Raghad

### Branch Strategy
Each team member works on their own branch.

- feature/charter-shatha
- feature/charter-fatima
- feature/charter-abd-al-hadi
- feature/charter-saja
- feature/charcter-raghad

The `main` branch is only for the final approved version.

### Commit Rules
- Write clear commit messages.
- Each commit should describe one change.

Examples:
- docs: add workflow section
- docs: update clean standards
- fix: correct typo

### Pull Request Workflow
1. Create a feature branch.
2. Complete the assigned task.
3. Commit the changes.
4. Push the branch to GitHub.
5. Create a Pull Request.
6. Wait for at least two approvals before merging into `main`.

### Merge Rules
- Do not push directly to the `main` branch.
- Merge conflicts should be resolved together by the team.


## Member 2 -  Clean Code Rules
1.	Write readable code.
2.	Use clear and meaningful names.
3.	Make each function perform only one task.
4.	Reduce duplication by reusing functions.
5.	Keep the code simple and straightforward.
6.	Break large functions and classes into smaller components.
7.	Pay attention to formatting and proper error handling.
8.	Make your code easy to maintain.
9.	Write tests for your code.
10.	Leave the code cleaner than you found it.

1) Write Readable Code
Your code should not only be understandable to you, but also to anyone who reads it later.

A reader should quickly understand:
•	What does the code do? 
•	Where does the logic start and end? 
•	What is the purpose of each part? 

Clean code should not force the reader to analyze every line. Instead, its purpose should be clear at first glance.
Example
X Unclear:
val x = 10
val y = 5
val z = x * y
Better Clearer:
val itemPrice = 10
val quantity = 5
val totalPrice = itemPrice * quantity
The variable names explain the purpose, making the code much easier to read.


2) Use Clear and Meaningful Names
Variable, function, and class names should clearly describe their purpose.
Good names reduce the need for comments because readers immediately understand what the code does.
Avoid vague names such as:
•	x 
•	temp 
•	data 
•	num 
•	value 
Instead, use descriptive names like:
•	studentName 
•	totalPrice 
•	isLoggedIn 
•	calculateAverage() 
Example
X
fun calc(a: Int, b: Int): Int {
    return a + b
}
Better
fun calculateSum(firstNumber: Int, secondNumber: Int): Int {
    return firstNumber + secondNumber
}
The second version is much clearer because the function and parameter names explain their purpose.


3) Make Each Function Do Only One Thing
This is one of the most important principles of Clean Code.
If a function reads data, performs calculations, prints results, and saves data to a database, it has too many responsibilities.
Rule:
Each function should have one single responsibility.
Example
X  A function doing multiple tasks:
fun processStudent(student: Student) {
    validateStudent(student)
    val average = calculateAverage(student)
    println(average)
    saveStudent(student)
}
This function:
•	Validates the data 
•	Calculates the average 
•	Prints the result 
•	Saves the student 
That's four different responsibilities.
 Better:
fun validateStudent(student: Student) { ... }

fun calculateAverage(student: Student): Double { ... }

fun printAverage(average: Double) { ... }

fun saveStudent(student: Student) { ... }
Now each function performs only one task, making the code easier to understand, test, and maintain.


4) Reduce Duplication by Reusing Functions
If you find yourself writing the same code repeatedly, it's usually a sign that something can be improved.
Code duplication makes maintenance harder because every change must be applied in multiple places.
Example
X Repetition:
val finalPrice1 = price1 - (price1 * 0.1)
val finalPrice2 = price2 - (price2 * 0.1)
val finalPrice3 = price3 - (price3 * 0.1)

 Better:

fun applyDiscount(price: Double): Double {
    return price - (price * 0.1)
}
Then use it like this:
val finalPrice1 = applyDiscount(price1)
val finalPrice2 = applyDiscount(price2)
val finalPrice3 = applyDiscount(price3)
If the discount formula changes, you only need to update it in one place.


5) Keep the Code Simple
Clean code avoids unnecessary complexity.
If there is a simpler way to express an idea, choose the simpler approach.
Try to:
•	Avoid overly complex conditions. 
•	Avoid deeply nested if statements. 
•	Avoid very long lines of code. 
•	Avoid clever tricks that reduce readability. 
Example
X Complex:
if ((age > 18 && hasId) || (isVip && !isBlocked && points > 100)) {
    ...
}
Better  Clearer:
val isAdultWithId = age > 18 && hasId
val isEligibleVip = isVip && !isBlocked && points > 100

if (isAdultWithId || isEligibleVip) {
    ...
}
The logic is identical, but much easier to read.


6) Break Large Functions and Classes into Smaller Parts
Very long functions or large classes become difficult to understand and maintain.
Instead:
•	Keep functions short. 
•	Give each class a single responsibility. 
•	Divide code into small, logical components. 
Example
Instead of:
fun handleStudent() {
    readInput()
    validateInput()
    calculateResult()
    printResult()
    saveResult()
}
You can separate each task:
fun readStudentInput() { ... }

fun validateStudentInput() { ... }

fun calculateStudentResult() { ... }

fun printStudentResult() { ... }

fun saveStudentResult() { ... }
Each function now has a clear purpose and is easier to maintain and test.


7) Pay Attention to Formatting and Error Handling
Clean code is not only about correct logic—it should also be well organized.
Good formatting improves readability, while proper error handling prevents runtime problems.
Important practices include:
•	Keep code neatly organized. 
•	Use consistent spacing and indentation. 
•	Follow a consistent brace style. 
•	Write clear error messages. 
•	Handle unexpected situations properly. 
Example
X fun divide(a: Int, b: Int): Int {
    return a / b
}
If b equals 0, the program will crash.
Better
fun divide(a: Int, b: Int): Int {
    require(b != 0) { "Divisor cannot be zero" }
    return a / b
}
Now the error is handled clearly and predictably.


8) Make Your Code Easy to Maintain
The goal isn't just to make your code work today—it should also be easy to modify in the future.
Clean code allows you to add new features or fix bugs without affecting unrelated parts of the program.
To improve maintainability:
•	Separate responsibilities. 
•	Use descriptive names. 
•	Reduce duplication. 
•	Write small functions. 
•	Avoid tightly coupling everything together. 
When each function has a clear responsibility, modifying one part becomes much easier.


9) Write Tests for Your Code
Testing is an essential part of Clean Code.
Tests verify that your code behaves as expected and protect it from future changes.
Benefits of testing:
•	Detects bugs quickly. 
•	Increases confidence when making changes. 
•	Prevents breaking existing functionality. 
•	Helps explain what a function is supposed to do. 
Example
fun add(a: Int, b: Int): Int {
    return a + b
}
A simple test:
assertEquals(5, add(2, 3))
This confirms that the function works correctly.


10) Leave the Code Cleaner Than You Found It
This principle is known as the Boy Scout Rule.
Whenever you modify a file or function, improve any small issues you notice instead of leaving them unchanged.
Examples of small improvements:
•	Rename a vague variable. 
•	Remove duplicated code. 
•	Split a long condition into meaningful variables. 
•	Improve formatting. 
•	Remove unused code. 
You don't need to rewrite the entire project—small continuous improvements make the whole project cleaner over time.

**4. Project Architecture & Repository Rules**

## 4.1 Project Architecture Overview

The project architecture is designed to maintain a clear separation of responsibilities, improve scalability, and make the system easier to understand, test, and maintain.

The team will follow a modular architecture where each component has a specific responsibility and communicates with other components through well-defined interfaces.

The main goals of this architecture are:

- Maintain clean and organized project structure.
- Reduce code dependency between components.
- Improve scalability for future features.
- Make testing and debugging easier.


## 4.2 Directory Structure

The project will follow the following directory organization:
src/
│
├── main/
│ │
│ ├── controller/
│ ├── service/
│ ├── repository/
│ ├── model/
│ ├── utils/
│ ├── config/
│ └── resources/
│
└── test/
├── unit/
└── integration/

### Directory Responsibilities:

### Controller Layer
Responsible for handling user requests, receiving inputs, and communicating with the service layer.

### Service Layer
Contains the main business logic and application rules.

### Repository Layer
Responsible for managing data access and communication with databases or external storage.

### Model Layer
Contains the main data objects and entities used throughout the application.

### Utils Layer
Contains reusable helper functions and common utilities.

### Config Layer
Contains application configuration files and environment settings.

### Test Layer
Contains automated tests to verify system functionality and reliability.



## 4.3 Package Organization

The project packages should follow a feature-based and responsibility-driven structure.

Example:
com.projectname

├── controller
├── service
├── repository
├── model
├── exception
├── security
├── utils
└── config
**Package names should be:**
- Clear and meaningful.
- Written using lowercase letters.
- Organized according to their responsibility.

## 4.4 Use Case Organization

The system functionality will be divided into independent use cases.

Each use case should represent a specific user goal or business operation.

Example:
usecases/

├── authentication/
│ ├── Login
│ └── Register
│
├── userManagement/
│ ├── CreateUser
│ └── UpdateUser
│
└── reporting/
├── GenerateReport
└── ExportData

**Benefits:**
- Easier feature development.
- Better separation of business logic.
- Simpler testing process.
- Easier future modifications.

## 4.5 Repository Management Rules

To maintain a clean and professional repository:
- Only required source files should be committed.
- Temporary files and generated files must not be pushed.
- Sensitive information such as passwords, API keys, and private credentials must never be stored in the repository.
- Every contribution must follow the defined Git workflow.
- All changes must be introduced through Pull Requests.

## 4.6 .gitignore Configuration

The `.gitignore` file is used to prevent unnecessary files from being uploaded to the repository.

## Purpose:
Keep the repository clean.
Avoid uploading machine-specific settings.
Prevent sensitive data exposure.
Reduce unnecessary repository size.

**The following files should be excluded:**

**gitignore**

1.IDE files
.idea/
.vscode/
*.iml

2.Build files
build/
target/
out/

3.Compiled files
*.class
*.jar

4.Logs
*.log

5.Environment files
.env
6.Operating system files
.DS_Store
Thumbs.db 

## 4.7 Architecture Maintenance Guidelines

The architecture should evolve with the project while maintaining consistency.

Before introducing major structural changes:

Discuss the change with team members.
Evaluate the impact on existing components.
Update documentation if needed.
Ensure the project build and tests remain successful.

5. Continuous Improvement & Team Growth
  
**"A team that learns together, leads together."**

**1. A Culture of Shared Learning**
We are not just a team that builds — we are a team that grows. 
Every late-night debugging session is an opportunity to sharpen our skills individually and collectively.

Knowledge belongs to the team, not the individual. 
We actively share what we learn — through quick technical walkthroughs, shared notes, and open discussions. If one of us discovers a better approach, the whole team adopts it.
We lift each other up. No question is too basic, and no insight is too small to share. A teammate who asks is learning; a teammate who explains is reinforcing. Both win.

**2. Feedback as a Tool for Excellence**
We treat feedback not as criticism, but as a hints we give each other that will help us alot.
When we review each other's work, we are not hunting for errors — we are raising the bar for the entire team. 
Every comment is a chance to teach, learn, and align on quality standards.
After every project delivery, we pause for a structured reflection: What went well? What slowed us down? What do we change next time? We celebrate progress and immediately act on lessons learned — no problem gets carried into the next phase unaddressed.
**3. Innovation as a Team Responsibility**
Continuous research and learning are not extras, they are essentials.
We dig deeper, question assumptions, and pursue better ideas — because the best solution today might not be the best solution tomorrow.

We challenge "good enough." When a cleaner solution exists, we pursue it. When a modern tool can save hours of work, we evaluate it. We are not attached to the familiar if the unfamiliar is clearly better.
Every team member owns the direction. Suggestions, experiments, and bold ideas are welcome from everyone — regardless of experience level. 

Our measure of success is not just what we deliver — it is who we become by the time we deliver it.
As **Robert C. Martin** reminds us in Clean Code:

"Honesty in small things is not a small thing."

We commit to being honest with each other — in feedback, in code reviews to become a great team togather.
That honesty is what turns a group of individuals into a team worth being part of.





