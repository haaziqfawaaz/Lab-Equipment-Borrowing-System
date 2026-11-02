# Lab Equipment Borrowing and Return System

A backend and database system developed in Core Java (JDK 21) using Object-Oriented Programming (OOP) principles and MySQL. This system manages the borrowing, return, and availability tracking of college laboratory equipment.

## Architecture

The project follows a clean **Layered Architecture** designed to separate concerns:

- **Model Layer (`src/model/`)**: Plain Old Java Objects (POJOs) representing domain entities (e.g., `User`, `Equipment`, `BorrowRecord`).
- **DAO Layer (`src/dao/`)**: Data Access Objects managing all CRUD operations and SQL queries to MySQL.
- **Service Layer (`src/service/`)**: Business logic and validation rules (e.g., equipment availability verification, borrowing limits, return status checks).
- **Database Layer (`src/database/`)**: Database connection management and JDBC configuration (`DatabaseConnection`).
- **Application Entry (`src/Main.java`)**: The main entry point to initialize and run the system.
- **Database Scripts (`database/schema.sql`)**: SQL scripts for initializing the MySQL database, tables, and constraints.

## Tech Stack

- **Language**: Java (JDK 21)
- **Database**: MySQL
- **Connectivity**: JDBC (MySQL Connector/J)
- **Design Pattern**: DAO (Data Access Object) Pattern & Layered Architecture
