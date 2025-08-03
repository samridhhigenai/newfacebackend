package com.faceattendance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeRegistrationRequest {

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    private String department;
    private String position;

    @NotBlank(message = "Face image is required")
    private String faceImage; // Base64 encoded image

    // Constructors
    public EmployeeRegistrationRequest() {}

    public EmployeeRegistrationRequest(String tenantId, String name, String email, String employeeId,
                                     String department, String position, String faceImage) {
        this.tenantId = tenantId;
        this.name = name;
        this.email = email;
        this.employeeId = employeeId;
        this.department = department;
        this.position = position;
        this.faceImage = faceImage;
    }

    // Getters and Setters
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

    public String getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(String faceImage) {
        this.faceImage = faceImage;
    }
}
