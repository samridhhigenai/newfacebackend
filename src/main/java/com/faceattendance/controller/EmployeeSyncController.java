package com.faceattendance.controller;

import com.faceattendance.model.Employee;
import com.faceattendance.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/employee-sync")
@CrossOrigin(origins = "*")
public class EmployeeSyncController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/test-sync")
    public ResponseEntity<?> testSyncEmployees(@RequestParam(defaultValue = "12345") String tenantId) {
        try {
            System.out.println("=== Test Employee Sync Request ===");
            System.out.println("Tenant ID: " + tenantId);

            // Create sample employee data
            List<Map<String, Object>> sampleEmployees = Arrays.asList(
                Map.of(
                    "externalId", "EMP001",
                    "name", "John Doe",
                    "email", "john.doe@company.com",
                    "employeeId", "EMP001",
                    "department", "Engineering",
                    "position", "Software Developer",
                    "isActive", true,
                    "hasFaceImage", false
                ),
                Map.of(
                    "externalId", "EMP002",
                    "name", "Jane Smith",
                    "email", "jane.smith@company.com",
                    "employeeId", "EMP002",
                    "department", "HR",
                    "position", "HR Manager",
                    "isActive", true,
                    "hasFaceImage", false
                ),
                Map.of(
                    "externalId", "EMP003",
                    "name", "Mike Johnson",
                    "email", "mike.johnson@company.com",
                    "employeeId", "EMP003",
                    "department", "Finance",
                    "position", "Accountant",
                    "isActive", true,
                    "hasFaceImage", false
                )
            );

            System.out.println("Sample employee count: " + sampleEmployees.size());

            int syncedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;

            for (Map<String, Object> empData : sampleEmployees) {
                try {
                    String externalId = (String) empData.get("externalId");
                    String email = (String) empData.get("email");

                    // Check if employee already exists by external ID or email
                    Employee existingEmployee = null;
                    if (externalId != null && !externalId.isEmpty()) {
                        existingEmployee = employeeService.findByExternalId(externalId);
                    }
                    if (existingEmployee == null && email != null && !email.isEmpty()) {
                        existingEmployee = employeeService.findByTenantIdAndEmail(tenantId, email);
                    }

                    if (existingEmployee != null) {
                        // Update existing employee
                        updateEmployeeFromData(existingEmployee, empData, tenantId);
                        employeeService.updateEmployee(existingEmployee);
                        updatedCount++;
                        System.out.println("Updated employee: " + existingEmployee.getName());
                    } else {
                        // Create new employee
                        Employee newEmployee = createEmployeeFromData(empData, tenantId);
                        employeeService.saveEmployee(newEmployee);
                        syncedCount++;
                        System.out.println("Created new employee: " + newEmployee.getName());
                    }
                } catch (Exception e) {
                    System.out.println("Error processing employee: " + e.getMessage());
                    skippedCount++;
                }
            }

            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Test sync completed successfully",
                "syncedCount", syncedCount,
                "updatedCount", updatedCount,
                "skippedCount", skippedCount,
                "totalProcessed", sampleEmployees.size()
            );

            System.out.println("=== Test Sync Response ===");
            System.out.println("Response: " + response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Test sync error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Test sync failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncEmployees(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== Employee Sync Request ===");
            System.out.println("Request: " + request);

            String tenantId = (String) request.get("tenantId");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> employees = (List<Map<String, Object>>) request.get("employees");

            if (tenantId == null || employees == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "tenantId and employees are required"
                ));
            }

            System.out.println("Tenant ID: " + tenantId);
            System.out.println("Employee count: " + employees.size());

            if (!employees.isEmpty()) {
                System.out.println("First employee data: " + employees.get(0));
                System.out.println("All employee data:");
                for (int i = 0; i < Math.min(employees.size(), 3); i++) {
                    System.out.println("Employee " + (i+1) + ": " + employees.get(i));
                }
            }

            int syncedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;

            for (Map<String, Object> empData : employees) {
                try {
                    String externalId = (String) empData.get("externalId");
                    String email = (String) empData.get("email");
                    
                    // Check if employee already exists by external ID or email
                    Employee existingEmployee = null;
                    if (externalId != null && !externalId.isEmpty()) {
                        existingEmployee = employeeService.findByExternalId(externalId);
                    }
                    if (existingEmployee == null && email != null && !email.isEmpty()) {
                        existingEmployee = employeeService.findByTenantIdAndEmail(tenantId, email);
                    }

                    if (existingEmployee != null) {
                        // Update existing employee
                        updateEmployeeFromData(existingEmployee, empData, tenantId);
                        employeeService.updateEmployee(existingEmployee);
                        updatedCount++;
                        System.out.println("Updated employee: " + existingEmployee.getName());
                    } else {
                        // Create new employee
                        Employee newEmployee = createEmployeeFromData(empData, tenantId);
                        employeeService.saveEmployee(newEmployee);
                        syncedCount++;
                        System.out.println("Created new employee: " + newEmployee.getName());
                    }
                } catch (Exception e) {
                    System.out.println("Error processing employee: " + e.getMessage());
                    skippedCount++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employees synced successfully");
            response.put("syncedCount", syncedCount);
            response.put("updatedCount", updatedCount);
            response.put("skippedCount", skippedCount);
            response.put("totalProcessed", employees.size());

            System.out.println("=== Sync Completed ===");
            System.out.println("Synced: " + syncedCount + ", Updated: " + updatedCount + ", Skipped: " + skippedCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ERROR: Employee sync failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to sync employees: " + e.getMessage()
            ));
        }
    }

    private Employee createEmployeeFromData(Map<String, Object> empData, String tenantId) {
        System.out.println("=== Creating Employee from Data ===");
        System.out.println("Input data: " + empData);

        Employee employee = new Employee();
        employee.setTenantId(tenantId);

        // Extract and validate fields
        String externalId = (String) empData.get("externalId");
        String name = (String) empData.get("name");
        String email = (String) empData.get("email");
        String employeeId = (String) empData.get("employeeId");
        String department = (String) empData.get("department");
        String position = (String) empData.get("position");

        System.out.println("Extracted fields:");
        System.out.println("  External ID: " + externalId);
        System.out.println("  Name: " + name);
        System.out.println("  Email: " + email);
        System.out.println("  Employee ID: " + employeeId);
        System.out.println("  Department: " + department);
        System.out.println("  Position: " + position);

        employee.setExternalId(externalId);
        employee.setName(name != null && !name.trim().isEmpty() ? name : "Unknown");
        employee.setEmail(email != null && !email.trim().isEmpty() ? email : "");
        employee.setEmployeeId(employeeId != null && !employeeId.trim().isEmpty() ? employeeId : "");
        employee.setDepartment(department != null && !department.trim().isEmpty() ? department : "Unknown");
        employee.setPosition(position != null && !position.trim().isEmpty() ? position : "Unknown");
        employee.setIsActive((Boolean) empData.getOrDefault("isActive", true));
        employee.setHasFaceImage((Boolean) empData.getOrDefault("hasFaceImage", false));
        employee.setIsSynced(true);

        System.out.println("Created employee: " + employee.getName() + " (" + employee.getEmployeeId() + ")");
        System.out.println("===================================");

        return employee;
    }

    private void updateEmployeeFromData(Employee employee, Map<String, Object> empData, String tenantId) {
        System.out.println("=== Updating Employee from Data ===");
        System.out.println("Input data: " + empData);

        // Extract and validate fields
        String name = (String) empData.get("name");
        String email = (String) empData.get("email");
        String employeeId = (String) empData.get("employeeId");
        String department = (String) empData.get("department");
        String position = (String) empData.get("position");

        System.out.println("Updating fields:");
        System.out.println("  Name: " + name);
        System.out.println("  Email: " + email);
        System.out.println("  Employee ID: " + employeeId);
        System.out.println("  Department: " + department);
        System.out.println("  Position: " + position);

        if (name != null && !name.trim().isEmpty()) {
            employee.setName(name);
        }
        if (email != null && !email.trim().isEmpty()) {
            employee.setEmail(email);
        }
        if (employeeId != null && !employeeId.trim().isEmpty()) {
            employee.setEmployeeId(employeeId);
        }
        if (department != null && !department.trim().isEmpty()) {
            employee.setDepartment(department);
        }
        if (position != null && !position.trim().isEmpty()) {
            employee.setPosition(position);
        }
        if (empData.get("isActive") != null) {
            employee.setIsActive((Boolean) empData.get("isActive"));
        }
        if (empData.get("externalId") != null) {
            employee.setExternalId((String) empData.get("externalId"));
        }

        employee.setIsSynced(true);

        System.out.println("Updated employee: " + employee.getName() + " (" + employee.getEmployeeId() + ")");
        System.out.println("===================================");
    }

    @GetMapping("/synced")
    public ResponseEntity<?> getSyncedEmployees(@RequestParam String tenantId) {
        try {
            List<Employee> employees = employeeService.getEmployeesByTenant(tenantId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "employees", employees
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to get employees: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/sync-from-external")
    public ResponseEntity<?> syncFromExternalAPI(
            @RequestParam(defaultValue = "12345") String tenantId,
            @RequestParam(required = false) String tenantLoginId,
            @RequestParam(required = false) String tenantPassword,
            @RequestParam(required = false) String accessToken,
            @RequestParam(defaultValue = "0") int skipCount,
            @RequestParam(defaultValue = "1000") int maxResultCount) {
        try {
            System.out.println("=== Sync From External API Request ===");
            System.out.println("Tenant ID: " + tenantId);
            System.out.println("Skip Count: " + skipCount);
            System.out.println("Max Result Count: " + maxResultCount);

            // External API URL with dynamic parameters and tenant filtering
            String externalApiUrl = "http://103.11.86.192:8083/api/services/app/Employees/GetAllEmployeeForFaceReco?SkipCount=" + skipCount + "&MaxResultCount=" + maxResultCount;

            // Add tenant filtering if tenantLoginId is provided
            if (tenantLoginId != null && !tenantLoginId.isEmpty()) {
                // Add tenant-specific filtering parameter
                externalApiUrl += "&TenantLoginId=" + java.net.URLEncoder.encode(tenantLoginId, "UTF-8");
                System.out.println("Added tenant filtering for: " + tenantLoginId);
            }

            // Create RestTemplate with proper configuration
            RestTemplate restTemplate = new RestTemplate();

            // Set timeout configurations
            restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.StringHttpMessageConverter(java.nio.charset.StandardCharsets.UTF_8));

            // Add error handler to handle HTTP errors gracefully
            restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                    return false; // Don't throw exceptions for HTTP errors, handle them manually
                }
            });

            // Step 1: Use provided access token or get fresh one if not provided
            String freshAccessToken = accessToken;
            if (freshAccessToken == null || freshAccessToken.isEmpty()) {
                System.out.println("No access token provided, getting fresh token...");
                freshAccessToken = getAccessTokenFromExternalAPI(tenantLoginId, tenantPassword);
                if (freshAccessToken == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to authenticate with external API using tenant credentials: " + tenantLoginId
                    ));
                }
            } else {
                System.out.println("Using provided access token: " + freshAccessToken.substring(0, Math.min(20, freshAccessToken.length())) + "...");
            }

            // Set headers for external API with fresh token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Java-Backend/1.0");
            headers.set("Authorization", "Bearer " + freshAccessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call external API
            System.out.println("=== CALLING EXTERNAL API ===");
            System.out.println("URL: " + externalApiUrl);
            System.out.println("Headers: " + headers);
            System.out.println("Request Method: GET");
            System.out.println("================================");

            ResponseEntity<Map> response;
            try {
                response = restTemplate.exchange(
                    externalApiUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
                );

                System.out.println("=== EXTERNAL API SUCCESS ===");
                System.out.println("Response Status: " + response.getStatusCode());
                System.out.println("Response Headers: " + response.getHeaders());
                System.out.println("Response Body: " + response.getBody());
                System.out.println("=============================");

            } catch (Exception e) {
                System.out.println("=== EXTERNAL API ERROR ===");
                System.out.println("Error Type: " + e.getClass().getSimpleName());
                System.out.println("Error Message: " + e.getMessage());
                System.out.println("Full Exception: ");
                e.printStackTrace();
                System.out.println("==========================");
                throw e;
            }

            // Extract employee data from response
            Map<String, Object> responseBody = response.getBody();
            System.out.println("=== RESPONSE BODY ANALYSIS ===");
            System.out.println("Response Body: " + responseBody);
            System.out.println("Response Body is null: " + (responseBody == null));
            if (responseBody != null) {
                System.out.println("Response Body keys: " + responseBody.keySet());
                System.out.println("Contains 'result' key: " + responseBody.containsKey("result"));
                System.out.println("Result value: " + responseBody.get("result"));
                System.out.println("Success value: " + responseBody.get("success"));
                if (responseBody.containsKey("error")) {
                    System.out.println("Error details: " + responseBody.get("error"));
                }
            }
            System.out.println("==============================");

            if (responseBody == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "External API returned null response"
                ));
            }

            // Check if external API returned an error
            Boolean success = (Boolean) responseBody.get("success");
            if (success != null && !success) {
                String errorMessage = "External API authentication failed. Please refresh your access token from Settings page.";
                if (responseBody.containsKey("error")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
                    if (error != null && error.containsKey("message")) {
                        String apiError = (String) error.get("message");
                        if (apiError != null && apiError.contains("Object reference not set")) {
                            errorMessage = "Access token expired or invalid. Please refresh your access token from Settings page.";
                        } else {
                            errorMessage = "External API error: " + apiError + ". Please refresh your access token from Settings page.";
                        }
                    }
                }
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errorMessage
                ));
            }

            if (!responseBody.containsKey("result")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid response from external API - no result key"
                ));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
            if (result == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "External API returned null result - this may indicate a server error on the external API"
                ));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) result.get("items");

            if (items == null || items.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "No employees found in external API",
                    "syncedCount", 0
                ));
            }

            System.out.println("Found " + items.size() + " items in external API");

            // Extract employee objects from items
            List<Map<String, Object>> employees = new ArrayList<>();
            for (Map<String, Object> item : items) {
                if (item.containsKey("employee")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> employee = (Map<String, Object>) item.get("employee");
                    employees.add(employee);
                    System.out.println("Extracted employee: " + employee.get("name") + " (" + employee.get("employeeCode") + ")");
                }
            }

            System.out.println("Found " + employees.size() + " employees in external API");

            int syncedCount = 0;
            int updatedCount = 0;
            int skippedCount = 0;

            // Process each employee
            for (Map<String, Object> empData : employees) {
                try {
                    String externalId = empData.get("id") != null ? empData.get("id").toString() : null;
                    String employeeCode = (String) empData.get("employeeCode");
                    String name = (String) empData.get("name");
                    String email = (String) empData.get("email");

                    System.out.println("=== Processing Employee ===");
                    System.out.println("External ID: " + externalId);
                    System.out.println("Employee Code: " + employeeCode);
                    System.out.println("Name: " + name);
                    System.out.println("Email: " + email);
                    System.out.println("Department: " + empData.get("department"));
                    System.out.println("Designation: " + empData.get("designation"));
                    System.out.println("===========================");

                    // Check if employee already exists
                    Employee existingEmployee = null;
                    if (externalId != null && !externalId.isEmpty()) {
                        existingEmployee = employeeService.findByExternalId(externalId);
                    }

                    if (existingEmployee != null) {
                        // Update existing employee
                        updateEmployeeFromExternalData(existingEmployee, empData, tenantId, tenantLoginId);
                        employeeService.updateEmployee(existingEmployee);
                        updatedCount++;
                        System.out.println("Updated employee: " + existingEmployee.getName());
                    } else {
                        // Create new employee
                        Employee newEmployee = createEmployeeFromExternalData(empData, tenantId, tenantLoginId);
                        employeeService.saveEmployee(newEmployee);
                        syncedCount++;
                        System.out.println("Created new employee: " + newEmployee.getName());
                    }
                } catch (Exception e) {
                    System.out.println("Error processing employee: " + e.getMessage());
                    e.printStackTrace();
                    skippedCount++;
                }
            }

            Map<String, Object> syncResponse = Map.of(
                "success", true,
                "message", "External API sync completed successfully",
                "syncedCount", syncedCount,
                "updatedCount", updatedCount,
                "skippedCount", skippedCount,
                "totalProcessed", employees.size()
            );

            System.out.println("=== External API Sync Response ===");
            System.out.println("Response: " + syncResponse);

            return ResponseEntity.ok(syncResponse);

        } catch (Exception e) {
            System.out.println("External API sync error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "External API sync failed: " + e.getMessage()
            ));
        }
    }

    private Employee createEmployeeFromExternalData(Map<String, Object> empData, String tenantId, String tenantLoginId) {
        Employee employee = new Employee();
        employee.setTenantId(tenantId);
        employee.setTenantLoginId(tenantLoginId);
        employee.setExternalId(empData.get("id") != null ? empData.get("id").toString() : null);

        // Map external API fields to our Employee model
        String name = (String) empData.get("name");
        if (name == null) {
            name = (String) empData.get("firstName");
        }
        employee.setName(name != null ? name.trim() : "Unknown");

        String email = (String) empData.get("email");
        if (email == null || "N/A".equals(email)) {
            email = "noemail@company.com";
        }
        employee.setEmail(email);

        // Use employeeCode from external API as employeeId
        String employeeCode = (String) empData.get("employeeCode");
        employee.setEmployeeId(employeeCode != null ? employeeCode : "EMP" + empData.get("id"));

        employee.setDepartment((String) empData.get("department"));
        employee.setPosition((String) empData.get("designation")); // designation maps to position
        employee.setIsActive(true);
        employee.setHasFaceImage(false);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        System.out.println("Created employee from external data: " + employee.getName() + " (" + employee.getEmployeeId() + ")");
        return employee;
    }

    private void updateEmployeeFromExternalData(Employee employee, Map<String, Object> empData, String tenantId, String tenantLoginId) {
        System.out.println("===================================");
        System.out.println("Updating employee from external data: " + employee.getName());

        // Update tenantLoginId if provided
        if (tenantLoginId != null && !tenantLoginId.isEmpty()) {
            employee.setTenantLoginId(tenantLoginId);
        }

        // Map external API fields to our Employee model
        String name = (String) empData.get("name");
        if (name == null) {
            name = (String) empData.get("firstName");
        }
        employee.setName(name != null ? name.trim() : employee.getName());

        String email = (String) empData.get("email");
        if (email != null && !"N/A".equals(email)) {
            employee.setEmail(email);
        }

        // Use employeeCode from external API as employeeId
        String employeeCode = (String) empData.get("employeeCode");
        if (employeeCode != null) {
            employee.setEmployeeId(employeeCode);
        }
        employee.setDepartment((String) empData.get("department"));
        employee.setPosition((String) empData.get("position"));
        employee.setUpdatedAt(LocalDateTime.now());

        System.out.println("Updated employee from external data: " + employee.getName() + " (" + employee.getEmployeeId() + ")");
        System.out.println("===================================");
    }

    /**
     * Get fresh access token from external API by authenticating with tenant credentials
     */
    private String getAccessTokenFromExternalAPI(String tenantLoginId, String tenantPassword) {
        try {
            System.out.println("=== GETTING FRESH ACCESS TOKEN FOR TENANT: " + tenantLoginId + " ===");

            if (tenantLoginId == null || tenantLoginId.isEmpty() || tenantPassword == null || tenantPassword.isEmpty()) {
                System.err.println("❌ Tenant credentials are missing. Cannot authenticate.");
                return null;
            }

            // External API login endpoint with query parameters
            String loginUrl = "http://103.11.86.192:8083/api/TokenAuth/MobileAuthenticate";

            // Build URL with query parameters using tenant credentials (URL encoded)
            String fullUrl = loginUrl + "?UserNameOrEmailAddress=" + java.net.URLEncoder.encode(tenantLoginId, "UTF-8") + "&Password=" + java.net.URLEncoder.encode(tenantPassword, "UTF-8");

            // Set headers for login request
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Java-Backend/1.0");

            HttpEntity<String> loginEntity = new HttpEntity<>(headers);

            // Create RestTemplate for login
            RestTemplate restTemplate = new RestTemplate();

            System.out.println("Login URL: " + fullUrl);
            System.out.println("Login Method: POST");

            // Call login API
            ResponseEntity<Map> loginResponse = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                loginEntity,
                Map.class
            );

            System.out.println("Login Response Status: " + loginResponse.getStatusCode());
            System.out.println("Login Response Body: " + loginResponse.getBody());

            if (loginResponse.getStatusCode().is2xxSuccessful() && loginResponse.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = loginResponse.getBody();

                if (responseBody.containsKey("result")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) responseBody.get("result");

                    if (result != null && result.containsKey("accessToken")) {
                        String accessToken = (String) result.get("accessToken");
                        System.out.println("Successfully obtained access token: " + accessToken.substring(0, 50) + "...");
                        return accessToken;
                    }
                }
            }

            System.out.println("Failed to get access token from login response");
            return null;

        } catch (Exception e) {
            System.out.println("Error getting access token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
