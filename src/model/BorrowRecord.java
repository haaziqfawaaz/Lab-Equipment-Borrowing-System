package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class representing a Borrow and Return Record.
 * Uses BigDecimal for financial fine precision.
 */
public class BorrowRecord {
    private int recordId;
    private int userId;
    private int equipmentId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private String status; // "BORROWED" or "RETURNED"
    private BigDecimal fineAmount;

    // Default constructor
    public BorrowRecord() {
        this.fineAmount = BigDecimal.ZERO;
    }

    // Constructor for creating a new borrow request
    public BorrowRecord(int userId, int equipmentId, LocalDate borrowDate, LocalDate dueDate) {
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = "BORROWED";
        this.fineAmount = BigDecimal.ZERO;
    }

    // Full constructor (for records retrieved from the database)
    public BorrowRecord(int recordId, int userId, int equipmentId, LocalDate borrowDate, 
                        LocalDate dueDate, LocalDate returnDate, String status, BigDecimal fineAmount) {
        this.recordId = recordId;
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.fineAmount = (fineAmount != null) ? fineAmount : BigDecimal.ZERO;
    }

    // Getters and Setters
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = (fineAmount != null) ? fineAmount : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", equipmentId=" + equipmentId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                ", fineAmount=" + fineAmount +
                '}';
    }
}
