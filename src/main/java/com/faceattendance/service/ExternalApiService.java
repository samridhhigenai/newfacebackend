package com.faceattendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Default credentials for authentication
    private static final String DEFAULT_USERNAME = "harshita@demomrr.com";
    private static final String DEFAULT_PASSWORD = "123qwe";

    public ExternalApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get a valid access token for external API calls
     */
    private String getValidAccessToken() {
        try {
            System.out.println("=== Getting Valid Access Token ===");

            // For now, always authenticate with external API to get fresh token
            System.out.println("Authenticating with external API to get fresh token...");
            return authenticateWithExternalAPI();

        } catch (Exception e) {
            System.err.println("Error getting access token: " + e.getMessage());
            // Fallback to direct authentication
            return authenticateWithExternalAPI();
        }
    }

    /**
     * Authenticate with external API to get fresh access token
     */
    private String authenticateWithExternalAPI() {
        try {
            System.out.println("=== Authenticating with External API ===");

            // Build URL with query parameters
            String fullUrl = AUTH_API_URL + "?UserNameOrEmailAddress=" + DEFAULT_USERNAME + "&Password=" + DEFAULT_PASSWORD;

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
     * Call external API to mark attendance with retry mechanism
     */
    public boolean markAttendanceExternal(String employeeId, boolean isCheckOut) {
        return markAttendanceExternalWithRetry(employeeId, isCheckOut, 0);
    }

    /**
     * Call external API to mark attendance with retry logic
     */
    private boolean markAttendanceExternalWithRetry(String employeeId, boolean isCheckOut, int retryCount) {
        final int maxRetries = 1; // Retry once if failed

        try {
            System.out.println("=== MARKING ATTENDANCE EXTERNALLY (Attempt " + (retryCount + 1) + ") ===");
            System.out.println("Employee ID: " + employeeId);
            System.out.println("Is Check Out: " + isCheckOut);

            // Get valid access token
            String accessToken = getValidAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("❌ Failed to obtain access token for external API");
                return false;
            }

            LocalDateTime now = LocalDateTime.now();

            // Create request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("punchDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
            requestBody.put("punchTime", now.format(DateTimeFormatter.ofPattern("HH:mm")));
            requestBody.put("machine", "face recognition");
            requestBody.put("location", "{\"lat\":\"28.6448\",\"lng\":\"77.216721\"}");

            // GPS locations array
            Map<String, String> gpsLocation = new HashMap<>();
            gpsLocation.put("lattitude", "28.6448");
            gpsLocation.put("longitude", "77.216721");
            gpsLocation.put("locationName", "Kashmiri Gate");
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
            System.out.println("=== 🚀 EXTERNAL API CALL TO MRR SYSTEM ===");
            System.out.println("🌐 URL: " + EXTERNAL_API_URL);
            System.out.println("👤 Employee ID: " + employeeId);
            System.out.println("⏰ Punch Time: " + requestBody.get("punchTime"));
            System.out.println("📅 Punch Date: " + requestBody.get("punchDate"));
            System.out.println("🔑 Access Token (first 30 chars): " + accessToken.substring(0, Math.min(30, accessToken.length())) + "...");
            System.out.println("📝 Full Request Body: " + objectMapper.writeValueAsString(requestBody));
            System.out.println("📡 Making HTTP POST request...");

            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                EXTERNAL_API_URL,
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            // Log the response details
            System.out.println("✅ API RESPONSE RECEIVED:");
            System.out.println("📊 Status Code: " + response.getStatusCode().value() + " (" + response.getStatusCode() + ")");
            System.out.println("📄 Response Headers: " + response.getHeaders());
            System.out.println("📋 Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("🎉 SUCCESS: External API call completed successfully!");
            } else {
                System.out.println("❌ ERROR: External API call failed with status: " + response.getStatusCode());
            }
            System.out.println("=== 🏁 END EXTERNAL API CALL ===");

            // Return true if successful (2xx status codes)
            return response.getStatusCode().is2xxSuccessful();

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
                return markAttendanceExternalWithRetry(employeeId, isCheckOut, retryCount + 1);
            } else {
                System.err.println("💥 Final failure: Could not mark attendance externally for employee " + employeeId + " after " + (maxRetries + 1) + " attempts");
                return false;
            }
        }
    }
}
