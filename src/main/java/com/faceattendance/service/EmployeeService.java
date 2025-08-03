package com.faceattendance.service;

import com.faceattendance.dto.EmployeeRegistrationRequest;
import com.faceattendance.dto.EmployeeResponse;
import com.faceattendance.model.Employee;
import com.faceattendance.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    /**
     * Register a new employee with face recognition
     */
    public EmployeeResponse registerEmployee(EmployeeRegistrationRequest request) {
        System.out.println("=== Employee Registration Started ===");
        System.out.println("Tenant ID: " + request.getTenantId());
        System.out.println("Employee ID: " + request.getEmployeeId());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Name: " + request.getName());

        // Check if employee already exists within the same tenant
        if (employeeRepository.existsByTenantIdAndEmail(request.getTenantId(), request.getEmail())) {
            System.out.println("ERROR: Employee with email already exists in this tenant");
            throw new RuntimeException("Employee with email already exists in this tenant");
        }

        if (employeeRepository.existsByTenantIdAndEmployeeId(request.getTenantId(), request.getEmployeeId())) {
            System.out.println("ERROR: Employee ID already exists in this tenant");
            throw new RuntimeException("Employee ID already exists in this tenant");
        }

        // Extract face encoding from image
        String faceEncoding;
        try {
            faceEncoding = faceRecognitionService.extractFaceEncoding(request.getFaceImage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to process face image: " + e.getMessage());
        }

        // Create new employee
        Employee employee = new Employee();
        employee.setTenantId(request.getTenantId());
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeId(request.getEmployeeId());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setFaceEncoding(faceEncoding);
        employee.setIsActive(true);

        // Save employee
        System.out.println("Attempting to save employee to MongoDB...");
        try {
            Employee savedEmployee = employeeRepository.save(employee);
            System.out.println("Employee saved successfully with ID: " + savedEmployee.getId());
            System.out.println("=== Employee Registration Completed ===");
            return convertToResponse(savedEmployee);
        } catch (Exception e) {
            System.out.println("ERROR: Failed to save employee to MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save employee: " + e.getMessage());
        }
    }

    /**
     * Register a new employee with MultipartFile image
     */
    public EmployeeResponse registerEmployee(EmployeeRegistrationRequest request, MultipartFile imageFile) {
        // Check if employee already exists within the same tenant
        if (employeeRepository.existsByTenantIdAndEmail(request.getTenantId(), request.getEmail())) {
            throw new RuntimeException("Employee with email already exists in this tenant");
        }

        if (employeeRepository.existsByTenantIdAndEmployeeId(request.getTenantId(), request.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists in this tenant");
        }

        // Convert MultipartFile to base64 string
        String faceEncoding = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                byte[] imageBytes = imageFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                faceEncoding = faceRecognitionService.extractFaceEncoding(base64Image);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process face image: " + e.getMessage());
            }
        }

        // Create new employee
        Employee employee = new Employee();
        employee.setTenantId(request.getTenantId());
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setEmployeeId(request.getEmployeeId());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        employee.setFaceEncoding(faceEncoding);
        employee.setIsActive(true);

        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);

        return convertToResponse(savedEmployee);
    }

    /**
     * Get all active employees by tenant
     */
    public List<EmployeeResponse> getAllActiveEmployeesByTenant(String tenantId) {
        return employeeRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all employees by tenant (including inactive)
     */
    public List<Employee> getEmployeesByTenant(String tenantId) {
        return employeeRepository.findByTenantId(tenantId);
    }

    /**
     * Get all active employees (deprecated - use tenant-specific version)
     */
    @Deprecated
    public List<EmployeeResponse> getAllActiveEmployees() {
        return employeeRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get employee by ID
     */
    public EmployeeResponse getEmployeeById(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToResponse(employee);
    }

    /**
     * Get employee by employee ID
     */
    public EmployeeResponse getEmployeeByEmployeeId(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToResponse(employee);
    }

    /**
     * Update employee information
     */
    public EmployeeResponse updateEmployee(String id, EmployeeRegistrationRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Update basic information
        employee.setName(request.getName());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());

        // Update face encoding if new image provided
        if (request.getFaceImage() != null && !request.getFaceImage().isEmpty()) {
            try {
                String faceEncoding = faceRecognitionService.extractFaceEncoding(request.getFaceImage());
                employee.setFaceEncoding(faceEncoding);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process face image: " + e.getMessage());
            }
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToResponse(updatedEmployee);
    }

    /**
     * Deactivate employee
     */
    public void deactivateEmployee(String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    /**
     * Get all employees with face encodings for recognition
     */
    public List<Employee> getEmployeesWithFaceEncoding() {
        return employeeRepository.findActiveEmployeesWithFaceEncoding();
    }

    /**
     * Find employee by face image
     */
    public Optional<Employee> findEmployeeByFace(String faceImage) {
        try {
            System.out.println("=== EMPLOYEE FACE RECOGNITION DEBUG ===");

            // Extract face encoding from input image
            String testEncoding = faceRecognitionService.extractFaceEncoding(faceImage);
            System.out.println("Test encoding extracted successfully");

            // Get all employees with face encodings
            List<Employee> employees = getEmployeesWithFaceEncoding();
            System.out.println("Found " + employees.size() + " employees with face encodings");

            if (employees.isEmpty()) {
                System.out.println("No employees with face encodings found");
                return Optional.empty();
            }

            // Print employee details for debugging
            for (Employee emp : employees) {
                System.out.println("Employee ID: " + emp.getId() + ", Name: " + emp.getName() + ", Has encoding: " + (emp.getFaceEncoding() != null));
            }

            // Prepare data for face matching
            List<String> knownEncodings = employees.stream()
                    .map(Employee::getFaceEncoding)
                    .collect(Collectors.toList());

            List<String> employeeIds = employees.stream()
                    .map(Employee::getId)
                    .collect(Collectors.toList());

            System.out.println("Prepared " + knownEncodings.size() + " encodings for matching");

            // Find best match
            FaceRecognitionService.FaceMatchResult result =
                    faceRecognitionService.findBestMatch(testEncoding, knownEncodings, employeeIds);

            System.out.println("Face matching completed. Match found: " + result.isMatch());

            if (result.isMatch()) {
                System.out.println("Match found! Employee ID: " + result.getEmployeeId() + ", Confidence: " + result.getConfidence());
                return employeeRepository.findById(result.getEmployeeId());
            }

            System.out.println("No match found above threshold");
            return Optional.empty();

        } catch (Exception e) {
            System.out.println("Face recognition error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to recognize face: " + e.getMessage());
        }
    }

    /**
     * Convert Employee entity to EmployeeResponse DTO
     */
    private EmployeeResponse convertToResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setEmail(employee.getEmail());
        response.setEmployeeId(employee.getEmployeeId());
        response.setDepartment(employee.getDepartment());
        response.setPosition(employee.getPosition());
        response.setIsActive(employee.getIsActive());
        response.setFaceEncoding(employee.getFaceEncoding());
        response.setHasFaceImage(employee.getHasFaceImage());
        response.setExternalId(employee.getExternalId());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    /**
     * Find employee by external ID
     */
    public Employee findByExternalId(String externalId) {
        return employeeRepository.findByExternalId(externalId);
    }

    /**
     * Find employee by tenant ID and email
     */
    public Employee findByTenantIdAndEmail(String tenantId, String email) {
        return employeeRepository.findByTenantIdAndEmail(tenantId, email).orElse(null);
    }

    /**
     * Save employee (for sync)
     */
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Update employee (for sync)
     */
    public Employee updateEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    /**
     * Get employees without face images
     */
    public List<Employee> getEmployeesWithoutFaceImage(String tenantId) {
        return employeeRepository.findByTenantIdAndHasFaceImageFalse(tenantId);
    }

    /**
     * Update employee face image status
     */
    public void updateFaceImageStatus(String employeeId, boolean hasFaceImage) {
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee != null) {
            employee.setHasFaceImage(hasFaceImage);
            employeeRepository.save(employee);
        }
    }

    /**
     * Update employee face image
     */
    public EmployeeResponse updateEmployeeFace(String employeeId, String tenantId, MultipartFile imageFile) {
        System.out.println("=== Update Employee Face Started ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Tenant ID: " + tenantId);

        // Find the employee by externalId (not MongoDB ID)
        Employee employee = employeeRepository.findByExternalId(employeeId);
        if (employee == null) {
            throw new RuntimeException("Employee not found with ID: " + employeeId);
        }

        // Verify tenant ID matches
        if (!employee.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Employee does not belong to the specified tenant");
        }

        // Extract face encoding from image
        String faceEncoding;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("Processing face image...");

                // Convert to base64 first
                String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());

                // Extract face encoding
                faceEncoding = faceRecognitionService.extractFaceEncoding(base64Image);

                if (faceEncoding == null || faceEncoding.isEmpty()) {
                    throw new RuntimeException("No face detected in the image");
                }

                // Save encoding and mark as having face image
                employee.setFaceEncoding(faceEncoding);
                employee.setHasFaceImage(true);

                System.out.println("Face encoding extracted successfully");
            } else {
                throw new RuntimeException("Image file is required");
            }
        } catch (Exception e) {
            System.out.println("ERROR: Face processing failed: " + e.getMessage());
            throw new RuntimeException("Face processing failed: " + e.getMessage());
        }

        // Save updated employee
        Employee updatedEmployee = employeeRepository.save(employee);
        System.out.println("Employee face updated successfully with ID: " + updatedEmployee.getId());

        return convertToResponse(updatedEmployee);
    }

    /**
     * Get all employees by tenant ID
     */
    public List<Employee> getEmployeesByTenantId(String tenantId) {
        System.out.println("Getting all employees for tenant: " + tenantId);
        List<Employee> employees = employeeRepository.findByTenantId(tenantId);
        System.out.println("Found " + employees.size() + " employees for tenant: " + tenantId);
        return employees;
    }

    /**
     * Delete employee by ID
     */
    public void deleteEmployee(String employeeId) {
        System.out.println("Deleting employee with ID: " + employeeId);
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            employeeRepository.delete(employee.get());
            System.out.println("Employee deleted successfully: " + employee.get().getName());
        } else {
            System.out.println("Employee not found with ID: " + employeeId);
        }
    }
}
