package com.faceattendance.controller;

import com.faceattendance.dto.EmployeeRegistrationRequest;
import com.faceattendance.dto.EmployeeResponse;
import com.faceattendance.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * Register a new employee
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerEmployee(
            @RequestParam("tenantId") String tenantId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("employeeId") String employeeId,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            EmployeeRegistrationRequest request = new EmployeeRegistrationRequest();
            request.setTenantId(tenantId);
            request.setName(name);
            request.setEmail(email);
            request.setEmployeeId(employeeId);
            request.setDepartment(department);
            request.setPosition(position);

            EmployeeResponse response = employeeService.registerEmployee(request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Registration failed", e.getMessage()));
        }
    }

    /**
     * Get all active employees by tenant login ID
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam("tenantLoginId") String tenantLoginId) {
        List<EmployeeResponse> employees = employeeService.getAllActiveEmployeesByTenantLoginId(tenantLoginId);
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String id) {
        try {
            EmployeeResponse employee = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Employee not found", e.getMessage()));
        }
    }

    /**
     * Get employee by employee ID
     */
    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<?> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        try {
            EmployeeResponse employee = employeeService.getEmployeeByEmployeeId(employeeId);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Employee not found", e.getMessage()));
        }
    }

    /**
     * Update employee information
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable String id,
                                          @Valid @RequestBody EmployeeRegistrationRequest request) {
        try {
            EmployeeResponse response = employeeService.updateEmployee(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Update failed", e.getMessage()));
        }
    }

    /**
     * Update employee face image
     */
    @PutMapping("/{id}/face")
    public ResponseEntity<?> updateEmployeeFace(
            @PathVariable String id,
            @RequestParam("tenantId") String tenantId,
            @RequestParam("image") MultipartFile image) {
        try {
            EmployeeResponse response = employeeService.updateEmployeeFace(id, tenantId, image);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Face update failed", e.getMessage()));
        }
    }

    /**
     * Deactivate employee
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateEmployee(@PathVariable String id) {
        try {
            employeeService.deactivateEmployee(id);
            return ResponseEntity.ok(new SuccessResponse("Employee deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Deactivation failed", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Employee service is running");
    }

    // Response classes
    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
