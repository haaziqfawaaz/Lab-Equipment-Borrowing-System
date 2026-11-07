package model;

/**
 * Abstract base class for people who use the LABTRACK system.
 * Stores common personal details shared by students and administrators.
 */
public abstract class Person {
    private String name;
    private String email;
    private String contact;

    protected Person() {
    }

    protected Person(String name, String email, String contact) {
        this.name = name;
        this.email = email;
        this.contact = contact;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
