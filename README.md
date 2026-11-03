# Lab Equipment Borrowing and Return System

A Java-based backend and database management system developed using Core Java (JDK 21), Object-Oriented Programming (OOP) principles, JDBC and MySQL.

The system manages laboratory equipment, student borrowing, equipment returns, availability tracking and overdue fines. It also provides REST-style HTTP APIs for frontend integration.

## 1. Project Objectives

- Manage students and administrator accounts.
- Authenticate users using password hashing.
- Provide role-based access for Students and Admins.
- Manage laboratory equipment and its availability.
- Record equipment borrowing and returns.
- Calculate fines for overdue equipment.
- Maintain borrowing history.
- Provide APIs for frontend communication.

## 2. Technology Stack

| Component             | Technology              |
| --------------------- | ----------------------- |
| Programming Language  | Java 21                 |
| Database              | MySQL 8.x               |
| Database Connectivity | JDBC                    |
| JDBC Driver           | MySQL Connector/J 8.4.0 |
| JSON Processing       | Gson 2.13.2             |
| HTTP Server           | Java HttpServer         |
| IDE                   | Antigravity / VS Code   |
| Database Tool         | MySQL Workbench         |
| Design Pattern        | DAO Pattern             |
| Architecture          | Layered Architecture    |

## 3. Project Architecture

The project follows a layered architecture.

- **Model Layer (`src/model/`)**: Contains POJO classes such as User, Equipment and BorrowRecord.
- **DAO Layer (`src/dao/`)**: Handles database operations using PreparedStatement.
- **Service Layer (`src/service/`)**: Implements business logic, validation and authorization.
- **Database Layer (`src/database/`)**: Manages MySQL database connectivity.
- **Security Layer (`src/security/`)**: Handles password hashing and verification.
- **API Layer (`src/api/`)**: Handles HTTP requests, authentication, sessions, equipment operations and borrowing operations.
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
- View all borrowing records.

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

| Table            | Purpose                                  |
| ---------------- | ---------------------------------------- |
| `users`          | Stores student and administrator details |
| `equipment`      | Stores laboratory equipment information  |
| `borrow_records` | Stores borrowing and return transactions |

The `users` table includes a `password_hash` column for password authentication.

## 6. Prerequisites

Install the following before running the project:

1. JDK 21.
2. MySQL Server 8.x.
3. MySQL Workbench (recommended).
4. Java IDE or VS Code.
5. MySQL Connector/J 8.4.0.
6. Gson 2.13.2.

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

Create the `bin` directory if it does not already exist:

```powershell
New-Item -ItemType Directory -Force bin
```

Compile the complete backend, including the HTTP API:

```powershell
javac --add-modules jdk.httpserver -cp "lib/mysql-connector-j-8.4.0.jar;lib/gson-2.13.2.jar" -d bin src/model/*.java src/database/*.java src/security/*.java src/dao/*.java src/service/*.java src/api/*.java src/Main.java
```

## 10. Run the Application

### Option A: Run the Console Application

```powershell
java -cp "bin;lib/mysql-connector-j-8.4.0.jar;lib/gson-2.13.2.jar" Main
```

The console menu provides student and administrator operations.

### Option B: Run the HTTP API Server

```powershell
java --add-modules jdk.httpserver -cp "bin;lib/mysql-connector-j-8.4.0.jar;lib/gson-2.13.2.jar" api.ApiServer
```

The API server runs at:

`http://localhost:8080`

Health check:

`GET http://localhost:8080/api/health`

## 11. API Documentation

All API requests and responses use JSON unless otherwise specified.

### Authentication API

| Method | Endpoint                   | Description                    | Access        |
| ------ | -------------------------- | ------------------------------ | ------------- |
| POST   | `/api/auth/register`       | Register a student             | Public        |
| POST   | `/api/auth/login`          | Login                          | Public        |
| POST   | `/api/auth/logout`         | Logout                         | Authenticated |
| POST   | `/api/auth/register/admin` | Register another administrator | Admin         |

Student registration request:

```json
{
  "name": "Student Name",
  "email": "student@example.com",
  "contact": "9876543210",
  "password": "YourPassword"
}
```

Login request:

```json
{
  "email": "student@example.com",
  "password": "YourPassword"
}
```

Admin registration uses the same JSON fields as student registration, but requires an existing administrator session.

### Equipment API

| Method | Endpoint                   | Description              | Access |
| ------ | -------------------------- | ------------------------ | ------ |
| GET    | `/api/equipment`           | View all equipment       | Public |
| GET    | `/api/equipment/available` | View available equipment | Public |
| POST   | `/api/equipment`           | Add equipment            | Admin  |
| PUT    | `/api/equipment`           | Update equipment         | Admin  |

Add equipment request:

```json
{
  "name": "Digital Voltmeter",
  "category": "Electronics",
  "totalQuantity": 3,
  "conditionStatus": "GOOD"
}
```

Update equipment request:

```json
{
  "equipmentId": 1,
  "name": "Digital Voltmeter",
  "category": "Electronics",
  "totalQuantity": 3,
  "availableQuantity": 3,
  "conditionStatus": "GOOD"
}
```

### Borrowing and Return API

| Method | Endpoint              | Description                     | Access        |
| ------ | --------------------- | ------------------------------- | ------------- |
| POST   | `/api/borrow`         | Borrow equipment                | Student       |
| POST   | `/api/borrow/return`  | Return equipment                | Authenticated |
| GET    | `/api/borrow/history` | View personal borrowing history | Student       |
| GET    | `/api/borrow/active`  | View active borrowings          | Student       |
| GET    | `/api/borrow/all`     | View all borrowing records      | Admin         |

Borrow request:

```json
{
  "equipmentId": 1
}
```

Return request:

```json
{
  "recordId": 1
}
```

## 12. Authentication and Session Management

After successful login, the API returns a session token.

Include this token in protected API requests:

```http
Authorization: Bearer YOUR_SESSION_TOKEN
```

The frontend should store the token for the logged-in session and send it with every protected request.

The backend uses session-based authentication and verifies administrator permissions for protected administrative operations.

## 13. Frontend Integration

The frontend communicates with the backend using HTTP requests and JSON.

API base URL:

```javascript
const API_BASE_URL = "http://localhost:8080/api";
```

For local development, the backend CORS configuration allows:

- `http://localhost:5500`
- `http://127.0.0.1:5500`

Example frontend login request:

```javascript
const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        email: "student@example.com",
        password: "YourPassword"
    })
});

const data = await response.json();
```

For protected operations, include the session token in the `Authorization` header.

## 14. Business Rules

- Only authenticated students can borrow equipment.
- Only administrators can add equipment and register other administrators.
- The standard borrowing period is seven days.
- The overdue fine is ₹5 per day.
- Equipment availability is updated during borrowing and returning.
- Students can access their own borrowing history.
- Administrators can view all borrowing records.
- Duplicate returns are prevented.
- Borrow and return operations use database transactions.

## 15. Testing

### Existing Backend Test Suite

The existing project README records successful completion of ten backend test cases:

| Test Case                      | Result |
| ------------------------------ | ------ |
| Student Registration           | PASS   |
| Duplicate Email Validation     | PASS   |
| Password Authentication        | PASS   |
| Incorrect Password Validation  | PASS   |
| Admin Authorization            | PASS   |
| Equipment Creation             | PASS   |
| Equipment Availability         | PASS   |
| Borrowing and Stock Validation | PASS   |
| Return and Fine Calculation    | PASS   |
| Duplicate Return Prevention    | PASS   |

**Previously documented test summary: 10/10 Tests Passed.**

### Additional HTTP API Verification

The following API operations have also been manually verified during development:

- Student registration and login.
- Admin login and admin registration.
- Login using a newly registered administrator.
- Rejection of student access to admin registration.
- Rejection of incorrect passwords.
- Rejection of duplicate email registration.
- Equipment creation and update.
- Borrowing, returning and borrowing history.
- Session invalidation after logout.

These API checks are separate from the ten-test backend suite above.

## 16. Security

- Passwords are stored as PBKDF2-based password hashes.
- PreparedStatement is used for database queries.
- Role-based authorization protects administrative operations.
- Session tokens are generated securely.
- Password hashes are not included in API user responses.
- Database credentials are stored locally.
- `db.properties` is excluded through `.gitignore`.

## 17. Future Enhancements

- Frontend integration.
- Improved user interface.
- Equipment search and filtering.
- Detailed administrative reports.
- Enhanced borrowing and return notifications.

## 18. Team Project

**Project:** Lab Equipment Borrowing and Return System

**Backend and Database:&#x20;**&#x68;aaziq fawaaz.k.g

**Frontend and Integration:** hari haran a&#x20;

---

Developed as a Java OOP and Database Management System project.
