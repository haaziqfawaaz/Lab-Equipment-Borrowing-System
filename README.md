# Lab Equipment Borrowing and Return System

A Java-based backend and database management system developed using Core Java (JDK 21), Object-Oriented Programming (OOP) principles, JDBC and MySQL.

The system manages laboratory equipment, student borrowing, equipment returns, availability tracking and overdue fines.

## 1. Project Objectives

- Manage students and administrator accounts.
- Authenticate users using password hashing.
- Provide role-based access for Students and Admins.
- Manage laboratory equipment and its availability.
- Record equipment borrowing and returns.
- Calculate fines for overdue equipment.
- Maintain borrowing history.

## 2. Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java 21 |
| Database | MySQL 8.x |
| Database Connectivity | JDBC |
| JDBC Driver | MySQL Connector/J 8.4.0 |
| IDE | Antigravity / VS Code |
| Database Tool | MySQL Workbench |
| Design Pattern | DAO Pattern |
| Architecture | Layered Architecture |

## 3. Project Architecture

The project follows a layered architecture.

- **Model Layer (`src/model/`)**: Contains POJO classes such as User, Equipment and BorrowRecord.
- **DAO Layer (`src/dao/`)**: Handles database operations using PreparedStatement.
- **Service Layer (`src/service/`)**: Implements business logic, validation and authorization.
- **Database Layer (`src/database/`)**: Manages MySQL database connectivity.
- **Security Layer (`src/security/`)**: Handles password hashing and verification.
- **Application Entry (`src/Main.java`)**: Provides the console-based application menu.
- **Database Scripts (`database/`)**: Contains the SQL database schema.

## 4. Main Modules

### Student Module
- Student registration.
- Login and logout.
- View available equipment.
- Borrow equipment.
- Return borrowed equipment.
- View personal borrowing history.

### Admin Module
- Admin login.
- Add and manage equipment.
- Register new administrators.
- Access administrative operations.

### Equipment Module
- Add laboratory equipment.
- View equipment details.
- Track total and available quantities.
- Update equipment information.

### Borrowing and Return Module
- Borrow available equipment.
- Generate borrowing records.
- Set a seven-day borrowing period.
- Process equipment returns.
- Prevent duplicate returns.
- Restore equipment availability.

### Fine Management
- Fine rate: ₹5 per overdue day.
- No fine for on-time returns.
- Fine calculated using the number of overdue days.

## 5. Database Design

The project uses three main tables:

| Table | Purpose |
|---|---|
| `users` | Stores student and administrator details |
| `equipment` | Stores laboratory equipment information |
| `borrow_records` | Stores borrowing and return transactions |

The `users` table includes a `password_hash` column for password authentication.

## 6. Prerequisites

Install the following before running the project:

1. JDK 21.
2. MySQL Server 8.x.
3. MySQL Workbench (recommended).
4. Java IDE or VS Code.
5. MySQL Connector/J 8.4.0.

## 7. Database Setup

1. Open MySQL Workbench.
2. Connect to your MySQL server.
3. Open `database/schema.sql`.
4. Execute the SQL script.
5. Verify that the `lab_equipment_db` database and its three tables are created.

The schema includes initial administrator, student and equipment records.

**Important:** The initial administrator account requires password configuration before its first login. Configure passwords securely; never commit password hashes or database credentials to GitHub.

## 8. Database Configuration

The project uses a local `db.properties` file for database connectivity.

1. Copy `db.properties.example`.
2. Create a local file named `db.properties` in the project root.
3. Enter your own MySQL connection details using the property names expected by `DatabaseConnection.java`.
4. Keep `db.properties` private.

Never upload `db.properties` to GitHub.

## 9. Compile the Project

Open the terminal in the project root directory.

Execute:

```powershell
javac -cp ".;lib/*;bin" -d bin src/model/*.java src/database/*.java src/dao/*.java src/security/*.java src/service/*.java src/Main.java
```

## 10. Run the Application

Execute:

```powershell
java -cp ".;lib/*;bin" Main
```

The console menu provides student and administrator operations.

## 11. Business Rules

- Only authenticated students can borrow equipment.
- Only administrators can add equipment and register other administrators.
- The standard borrowing period is seven days.
- The overdue fine is ₹5 per day.
- Equipment availability is updated during borrowing and returning.
- Students can access their own borrowing history.
- Duplicate returns are prevented.

## 12. Automated Testing

The backend automated test suite has successfully passed all ten test cases.

| Test Case | Result |
|---|---|
| Student Registration | PASS |
| Duplicate Email Validation | PASS |
| Password Authentication | PASS |
| Incorrect Password Validation | PASS |
| Admin Authorization | PASS |
| Equipment Creation | PASS |
| Equipment Availability | PASS |
| Borrowing and Stock Validation | PASS |
| Return and Fine Calculation | PASS |
| Duplicate Return Prevention | PASS |

**Test Summary: 10/10 Tests Passed**

## 13. Security

- Passwords are stored as PBKDF2-based password hashes.
- PreparedStatement is used for database queries.
- Role-based authorization protects administrative operations.
- Database credentials are stored locally.
- `db.properties` is excluded through `.gitignore`.

## 14. Future Enhancements

- Frontend integration.
- Improved user interface.
- Equipment search and filtering.
- Detailed administrative reports.
- Enhanced borrowing and return notifications.

## 15. Team Project

**Project:** Lab Equipment Borrowing and Return System

**Backend and Database:** Hariharan R.

**Frontend and Integration:** Team Member

---

Developed as a Java OOP and Database Management System project.
