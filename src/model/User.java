package model;

import java.time.LocalDateTime;

/**
 * Model class representing a User (Student or Admin).
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String role; // "STUDENT" or "ADMIN"
    private String contact;
    private LocalDateTime createdAt;

    // Default constructor
    public User() {
    }

    // Constructor for creating new users (before ID is assigned by the database)
    public User(String name, String email, String role, String contact) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.contact = contact;
    }

    // Full constructor (for users fetched from the database)
    public User(int userId, String name, String email, String role, String contact, LocalDateTime createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.contact = contact;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", contact='" + contact + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
