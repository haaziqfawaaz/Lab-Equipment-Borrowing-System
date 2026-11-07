package model;

import java.time.LocalDateTime;

/**
 * Model class representing a User (Student or Admin).
 * Inherits common personal details from the abstract Person class.
 */
public class User extends Person {
    private int userId;
    private String role;
    private String passwordHash;
    private LocalDateTime createdAt;

    // Default constructor required by Gson and DAO mapping.
    public User() {
        super();
    }

    // Constructor for creating new users.
    public User(String name, String email, String role, String contact) {
        super(name, email, contact);
        this.role = role;
    }

    // Constructor including password hash.
    public User(String name, String email, String role,
                String contact, String passwordHash) {
        super(name, email, contact);
        this.role = role;
        this.passwordHash = passwordHash;
    }

    // Full constructor for users fetched from database.
    public User(int userId, String name, String email, String role,
                String contact, LocalDateTime createdAt) {
        super(name, email, contact);
        this.userId = userId;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", role='" + role + '\'' +
                ", contact='" + getContact() + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
