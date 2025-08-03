package com.faceattendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceResponse {

    private String id;
    private String employeeId;
    private String employeeName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDate attendanceDate;
    private String status;
    private Double confidenceScore;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public AttendanceResponse() {}

    public AttendanceResponse(String id, String employeeId, String employeeName,
                             LocalDateTime checkInTime, LocalDateTime checkOutTime,
                             LocalDate attendanceDate, String status,
                             Double confidenceScore, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.confidenceScore = confidenceScore;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
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
}
