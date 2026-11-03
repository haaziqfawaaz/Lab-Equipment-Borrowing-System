
-- ====================================================================
-- Database Schema: Lab Equipment Borrowing and Return System
-- DBMS: MySQL 8.x
-- ====================================================================

-- 1. Create and select database
CREATE DATABASE IF NOT EXISTS lab_equipment_db;
USE lab_equipment_db;

-- 2. Users Table
-- Stores both Students and Admins
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    contact VARCHAR(20),
    password_hash VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. Equipment Table
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    total_quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    condition_status VARCHAR(50) DEFAULT 'GOOD',

    CONSTRAINT chk_total_qty
        CHECK (total_quantity >= 0),

    CONSTRAINT chk_avail_qty
        CHECK (
            available_quantity >= 0
            AND available_quantity <= total_quantity
        )
) ENGINE=InnoDB;

-- 4. Borrow Records Table
CREATE TABLE IF NOT EXISTS borrow_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    equipment_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status ENUM('BORROWED', 'RETURNED')
        NOT NULL DEFAULT 'BORROWED',
    fine_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_borrow_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_borrow_equipment
        FOREIGN KEY (equipment_id)
        REFERENCES equipment(equipment_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ====================================================================
-- 5. Seed Initial Data
-- Safe to re-run using INSERT IGNORE
-- ====================================================================

-- Default System Administrator
-- Password must be configured separately.
INSERT IGNORE INTO users
    (user_id, name, email, role, contact)
VALUES
    (1, 'System Administrator',
     'admin@lab.edu', 'ADMIN', '+91-9876543210');

-- Starter Students
INSERT IGNORE INTO users
    (user_id, name, email, role, contact)
VALUES
    (2, 'Aarav Sharma',
     'aarav.sharma@student.edu', 'STUDENT', '+91-9811122233'),

    (3, 'Diya Patel',
     'diya.patel@student.edu', 'STUDENT', '+91-9822233344');

-- Initial Lab Equipment
INSERT IGNORE INTO equipment
    (equipment_id, name, category, total_quantity,
     available_quantity, condition_status)
VALUES
    (1, 'Digital Storage Oscilloscope (50MHz)',
     'Electronics', 5, 5, 'GOOD'),

    (2, 'Digital Multimeter (True RMS)',
     'Electronics', 10, 10, 'GOOD'),

    (3, 'Function Generator (10MHz)',
     'Electronics', 4, 4, 'GOOD'),

    (4, 'Binocular Compound Microscope',
     'Biology', 6, 6, 'GOOD'),

    (5, 'Digital Analytical Balance (0.1mg)',
     'Chemistry', 3, 3, 'GOOD');

-- ====================================================================
-- END OF DATABASE SCHEMA
-- ====================================================================
