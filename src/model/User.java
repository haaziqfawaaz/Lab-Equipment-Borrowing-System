
package model;

import java.time.LocalDateTime;

/**
 * Model class representing a User (Student or Admin).
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String role;
    private String contact;
    private String passwordHash;
    private LocalDateTime createdAt;

    // Default constructor
    public User() {
    }

    // Constructor for creating new users
    public User(String name, String email, String role, String contact) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.contact = contact;
    }

    // Constructor including password hash
    public User(String name, String email, String role,
                String contact, String passwordHash) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.contact = contact;
        this.passwordHash = passwordHash;
    }

    // Full constructor for users fetched from database
    public User(int userId, String name, String email, String role,
                String contact, LocalDateTime createdAt) {
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
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", contact='" + contact + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
