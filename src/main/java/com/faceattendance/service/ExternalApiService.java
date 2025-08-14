package com.faceattendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExternalApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Remove TenantService dependency for now - we'll handle tokens directly

    private static final String EXTERNAL_API_URL = "http://103.11.86.192:8083/api/services/app/MarkAttendances/CreatePunchForMRR";
    private static final String AUTH_API_URL = "http://103.11.86.192:8083/api/TokenAuth/MobileAuthenticate";

    public ExternalApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get a valid access token for external API calls using tenant credentials
     */
    private String getValidAccessToken(String tenantLoginId, String tenantPassword) {
        try {
            System.out.println("=== Getting Valid Access Token for Tenant: " + tenantLoginId + " ===");

            // Always authenticate with external API to get fresh token using tenant credentials
            System.out.println("Authenticating with external API using tenant credentials...");
            return authenticateWithExternalAPI(tenantLoginId, tenantPassword);

        } catch (Exception e) {
            System.err.println("Error getting access token for tenant " + tenantLoginId + ": " + e.getMessage());
            // Fallback to direct authentication with tenant credentials
            return authenticateWithExternalAPI(tenantLoginId, tenantPassword);
        }
    }

    /**
     * Authenticate with external API to get fresh access token using tenant credentials
     */
    private String authenticateWithExternalAPI(String tenantLoginId, String tenantPassword) {
        try {
            System.out.println("=== Authenticating with External API for Tenant: " + tenantLoginId + " ===");

            if (tenantLoginId == null || tenantLoginId.isEmpty() || tenantPassword == null || tenantPassword.isEmpty()) {
                System.err.println("❌ Tenant credentials are missing. Cannot authenticate.");
                return null;
            }

            // Build URL with query parameters using tenant credentials
            String fullUrl = AUTH_API_URL + "?UserNameOrEmailAddress=" + java.net.URLEncoder.encode(tenantLoginId, "UTF-8") + "&Password=" + java.net.URLEncoder.encode(tenantPassword, "UTF-8");

            // Set headers for login request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("User-Agent", "Java-Backend/1.0");

            HttpEntity<String> loginEntity = new HttpEntity<>(headers);

            System.out.println("Auth URL: " + fullUrl);

            // Make the authentication request
            ResponseEntity<Map<String, Object>> authResponse = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                loginEntity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            System.out.println("Auth Response Status: " + authResponse.getStatusCode());
            System.out.println("Auth Response Body: " + authResponse.getBody());

            if (authResponse.getStatusCode().is2xxSuccessful() && authResponse.getBody() != null) {
                Map<String, Object> responseBody = authResponse.getBody();

                if (responseBody.containsKey("result")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) responseBody.get("result");

                    if (result != null && result.containsKey("accessToken")) {
                        String accessToken = (String) result.get("accessToken");
                        System.out.println("✅ Successfully obtained access token");
                        return accessToken;
                    }
                }
            }

            System.err.println("❌ Failed to obtain access token from authentication response");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Authentication with external API failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Call external API to mark attendance with retry mechanism using tenant credentials
     */
    public boolean markAttendanceExternal(String employeeId, boolean isCheckOut, String tenantLoginId, String tenantPassword) {
        return markAttendanceExternalWithRetry(employeeId, isCheckOut, tenantLoginId, tenantPassword, 0);
    }

    /**
     * Call external API to mark attendance with retry logic using tenant credentials
     */
    private boolean markAttendanceExternalWithRetry(String employeeId, boolean isCheckOut, String tenantLoginId, String tenantPassword, int retryCount) {
        final int maxRetries = 1; // Retry once if failed

        try {
            System.out.println("=== MARKING ATTENDANCE EXTERNALLY (Attempt " + (retryCount + 1) + ") ===");
            System.out.println("Employee ID: " + employeeId);
            System.out.println("Is Check Out: " + isCheckOut);
            System.out.println("Tenant Login ID: " + tenantLoginId);

            // Get valid access token using tenant credentials
            String accessToken = getValidAccessToken(tenantLoginId, tenantPassword);
            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("❌ Failed to obtain access token for external API using tenant credentials: " + tenantLoginId);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("punchDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
            requestBody.put("punchTime", now.format(DateTimeFormatter.ofPattern("HH:mm")));
            requestBody.put("machine", "face recognition");
            requestBody.put("location", "Kashmiri Gate");

            // GPS locations array
            Map<String, String> gpsLocation = new HashMap<>();
            gpsLocation.put("lattitude", "28.6448");
            gpsLocation.put("longitude", "77.216721");
            gpsLocation.put("locationName", "");
            requestBody.put("gpsLocations", List.of(gpsLocation));

            requestBody.put("employeeId", employeeId);
            requestBody.put("inOut", isCheckOut ? "OUT" : "IN");
            requestBody.put("deviceName", "test");
            requestBody.put("id", 0);

            // Create headers with dynamic token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            System.out.println("Using access token: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");

            // Create request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Log the request details
            System.out.println("=== 🚀 MarkAttendances/CreatePunchForMRR API REQUEST ===");
            System.out.println("🌐 Full URL: " + EXTERNAL_API_URL);
            System.out.println("📝 Request Method: POST");
            System.out.println("📋 Request Headers:");
            System.out.println("   - Content-Type: application/json");
            System.out.println("   - Authorization: Bearer " + accessToken.substring(0, Math.min(30, accessToken.length())) + "...");
            System.out.println("📊 Request Body Key-Value Pairs:");
            System.out.println("   - employeeId: " + requestBody.get("employeeId"));
            System.out.println("   - punchDate: " + requestBody.get("punchDate"));
            System.out.println("   - punchTime: " + requestBody.get("punchTime"));
            System.out.println("   - machine: " + requestBody.get("machine"));
            System.out.println("   - location: " + requestBody.get("location"));
            System.out.println("   - gpsLocations: " + requestBody.get("gpsLocations"));
            System.out.println("   - inOut: " + requestBody.get("inOut"));
            System.out.println("   - deviceName: " + requestBody.get("deviceName"));
            System.out.println("   - id: " + requestBody.get("id"));
            System.out.println("📄 Complete Request Body JSON: " + objectMapper.writeValueAsString(requestBody));
            System.out.println("📡 Making HTTP POST request to MRR system...");

            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                EXTERNAL_API_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            // Log the response details
            System.out.println("=== 📥 MarkAttendances/CreatePunchForMRR API RESPONSE ===");
            System.out.println("📊 Status Code: " + response.getStatusCode().value() + " (" + response.getStatusCode() + ")");
            System.out.println("📋 Response Headers: " + response.getHeaders());
            System.out.println("📄 Response Body: " + response.getBody());
            System.out.println("⏱️ Response Time: " + java.time.LocalDateTime.now());

            // Parse response body for detailed analysis
            try {
                String responseBody = response.getBody();
                ObjectMapper responseMapper = new ObjectMapper();
                JsonNode responseJson = responseMapper.readTree(responseBody);

                if (response.getStatusCode().is2xxSuccessful()) {
                    // Check for success in response body
                    if (responseJson.has("success") && responseJson.get("success").asBoolean()) {
                        System.out.println("=== ✅ MarkAttendances/CreatePunchForMRR SUCCESS ===");
                        System.out.println("🎉 External attendance marked successfully in MRR system!");
                        System.out.println("� Result: " + responseJson.get("result").asText());
                        System.out.println("�👤 Employee ID: " + requestBody.get("employeeId"));
                        System.out.println("⏰ Punch Time: " + requestBody.get("punchTime"));
                        System.out.println("📍 In/Out: " + requestBody.get("inOut"));
                        System.out.println("✅ Attendance marked in external MRR system successfully!");
                        return true;
                    } else {
                        System.out.println("=== ❌ MarkAttendances/CreatePunchForMRR FAILED (Success=false) ===");
                        System.out.println("📄 Response indicates failure despite 2xx status code");
                        System.out.println("📝 Response: " + responseBody);
                        return false;
                    }
                } else {
                    System.out.println("=== ❌ MarkAttendances/CreatePunchForMRR FAILED ===");
                    System.out.println("📊 HTTP Status: " + response.getStatusCode());

                    // Handle different error types
                    if (responseJson.has("error")) {
                        JsonNode error = responseJson.get("error");
                        String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";

                        System.out.println("🔍 Error Analysis:");
                        System.out.println("   - Error Message: " + errorMessage);

                        // Specific error handling
                        if (errorMessage.contains("Multiple employees exist")) {
                            System.out.println("   - Error Type: DUPLICATE_EMPLOYEE");
                            System.out.println("   - Issue: Multiple employees found with same ID");
                        } else if (errorMessage.contains("Employee does not exist")) {
                            System.out.println("   - Error Type: EMPLOYEE_NOT_FOUND");
                            System.out.println("   - Issue: Employee ID not found in external system");
                        } else if (errorMessage.contains("Incorrect Punch Date")) {
                            System.out.println("   - Error Type: INVALID_DATE");
                            System.out.println("   - Issue: Date format or value is incorrect");
                        } else if (errorMessage.contains("SQL Execution Failed")) {
                            System.out.println("   - Error Type: SQL_ERROR");
                            System.out.println("   - Issue: Database execution failed");
                        } else {
                            System.out.println("   - Error Type: UNKNOWN");
                        }

                        System.out.println("📄 Full Error Response: " + responseBody);
                    } else {
                        System.out.println("📄 Unexpected error response format: " + responseBody);
                    }
                    return false;
                }
            } catch (Exception e) {
                System.out.println("❌ Failed to parse response JSON: " + e.getMessage());
                System.out.println("📄 Raw response: " + response.getBody());
                System.out.println("=== 🏁 END MarkAttendances/CreatePunchForMRR API CALL ===");
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ External API call failed (attempt " + (retryCount + 1) + "): " + e.getMessage());
            e.printStackTrace();

            // Retry logic
            if (retryCount < maxRetries) {
                System.out.println("🔄 Retrying external API call for employee " + employeeId + "...");
                try {
                    Thread.sleep(2000); // Wait 2 seconds before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return markAttendanceExternalWithRetry(employeeId, isCheckOut, tenantLoginId, tenantPassword, retryCount + 1);
            } else {
                System.err.println("💥 Final failure: Could not mark attendance externally for employee " + employeeId + " after " + (maxRetries + 1) + " attempts");
                return false;
            }
        }
    }
}
