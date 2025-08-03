package com.faceattendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "attendances")
public class Attendance {

    @Id
    private String id;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @DBRef
    private Employee employee;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private LocalDate attendanceDate;

    private AttendanceStatus status;

    private Double confidenceScore;

    private String checkInImagePath;

    private String checkOutImagePath;

    private String notes;

    @CreatedDate
    private LocalDateTime createdAt;

    // Constructors
    public Attendance() {}

    public Attendance(Employee employee, LocalDateTime checkInTime, LocalDate attendanceDate, 
                     AttendanceStatus status, Double confidenceScore) {
        this.employee = employee;
        this.checkInTime = checkInTime;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.confidenceScore = confidenceScore;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getCheckInImagePath() {
        return checkInImagePath;
    }

    public void setCheckInImagePath(String checkInImagePath) {
        this.checkInImagePath = checkInImagePath;
    }

    public String getCheckOutImagePath() {
        return checkOutImagePath;
    }

    public void setCheckOutImagePath(String checkOutImagePath) {
        this.checkOutImagePath = checkOutImagePath;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Enum for attendance status
    public enum AttendanceStatus {
        PRESENT,
        LATE,
        ABSENT,
        HALF_DAY
    }
}
