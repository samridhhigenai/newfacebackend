package com.faceattendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String employeeId;

    private String department;

    private String position;

    private String faceEncoding;

    private Boolean isActive = true;

    // External sync fields
    private String externalId; // ID from external API
    private Boolean hasFaceImage = false; // Whether face image is available
    private Boolean isSynced = false; // Whether synced from external API

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public Employee() {}

    public Employee(String tenantId, String name, String email, String employeeId, String department, String position) {
        this.tenantId = tenantId;
        this.name = name;
        this.email = email;
        this.employeeId = employeeId;
        this.department = department;
        this.position = position;
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

    public String getFaceEncoding() {
        return faceEncoding;
    }

    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
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

    // External sync getters and setters
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Boolean getHasFaceImage() {
        return hasFaceImage;
    }

    public void setHasFaceImage(Boolean hasFaceImage) {
        this.hasFaceImage = hasFaceImage;
    }

    public Boolean getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(Boolean isSynced) {
        this.isSynced = isSynced;
    }
}
