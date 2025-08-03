package com.faceattendance.dto;

import java.time.LocalDateTime;

public class EmployeeResponse {

    private String id;
    private String name;
    private String email;
    private String employeeId;
    private String department;
    private String position;
    private Boolean isActive;
    private String faceEncoding;
    private Boolean hasFaceImage;
    private String externalId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public EmployeeResponse() {}

    public EmployeeResponse(String id, String name, String email, String employeeId,
                           String department, String position, Boolean isActive,
                           String faceEncoding, Boolean hasFaceImage, String externalId,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.employeeId = employeeId;
        this.department = department;
        this.position = position;
        this.isActive = isActive;
        this.faceEncoding = faceEncoding;
        this.hasFaceImage = hasFaceImage;
        this.externalId = externalId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFaceEncoding() {
        return faceEncoding;
    }

    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
    }

    public Boolean getHasFaceImage() {
        return hasFaceImage;
    }

    public void setHasFaceImage(Boolean hasFaceImage) {
        this.hasFaceImage = hasFaceImage;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
