package com.faceattendance.service;

import com.faceattendance.dto.AttendanceRequest;
import com.faceattendance.dto.AttendanceResponse;
import com.faceattendance.model.Attendance;
import com.faceattendance.model.Employee;
import com.faceattendance.repository.AttendanceRepository;
import com.faceattendance.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * Record attendance using face recognition
     */
    public AttendanceResponse recordAttendance(AttendanceRequest request) {
        // Find employee by face recognition
        Optional<Employee> employeeOpt = employeeService.findEmployeeByFace(request.getFaceImage());

        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Face not recognized. Please ensure you are registered.");
        }

        Employee employee = employeeOpt.get();
        LocalDate today = LocalDate.now();

        // Check if attendance already recorded for today (get latest record)
        List<Attendance> existingAttendances = attendanceRepository
                .findByEmployeeAndAttendanceDateOrderByCreatedAtDesc(employee, today);

        if (!existingAttendances.isEmpty()) {
            // Get the latest attendance record
            Attendance latestAttendance = existingAttendances.get(0);
            if (latestAttendance.getCheckOutTime() == null && request.isCheckOut()) {
                latestAttendance.setCheckOutTime(LocalDateTime.now());
                latestAttendance.setCheckOutImagePath(request.getImagePath());
                Attendance updatedAttendance = attendanceRepository.save(latestAttendance);
                return convertToResponse(updatedAttendance);
            } else {
                throw new RuntimeException("Attendance already recorded for today");
            }
        }

        // Calculate confidence score
        double confidenceScore = calculateConfidenceScore(employee.getFaceEncoding(), request.getFaceImage());

        // Determine attendance status based on time
        Attendance.AttendanceStatus status = determineAttendanceStatus(LocalTime.now());

        // Create new attendance record
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setTenantId(employee.getTenantId()); // Set tenantId from employee
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setAttendanceDate(today);
        attendance.setStatus(status);
        attendance.setConfidenceScore(confidenceScore);
        attendance.setCheckInImagePath(request.getImagePath());
        attendance.setNotes(request.getNotes());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return convertToResponse(savedAttendance);
    }

    /**
     * Record attendance using face recognition with image upload
     */
    public AttendanceResponse recordAttendanceWithImage(MultipartFile imageFile) {
        try {
            // Convert MultipartFile to base64 string
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Find employee by face recognition
            Optional<Employee> employeeOpt = employeeService.findEmployeeByFace(base64Image);

            if (employeeOpt.isEmpty()) {
                throw new RuntimeException("Face not recognized. Please ensure you are registered.");
            }

            Employee employee = employeeOpt.get();
            LocalDate today = LocalDate.now();

            // Check if attendance already recorded for today (get latest record)
            List<Attendance> existingAttendances = attendanceRepository
                    .findByEmployeeAndAttendanceDateOrderByCreatedAtDesc(employee, today);

            if (!existingAttendances.isEmpty()) {
                // Get the latest attendance record
                Attendance latestAttendance = existingAttendances.get(0);

                if (latestAttendance.getCheckOutTime() == null) {
                    // Update check-out time for the latest record
                    latestAttendance.setCheckOutTime(LocalDateTime.now());
                    Attendance updatedAttendance = attendanceRepository.save(latestAttendance);

                    // Call external API for check-out using tenant credentials
                    // Note: For now, skipping external API call as we need tenant credentials
                    // TODO: Pass tenant credentials from request or employee data
                    System.out.println("External API call skipped - tenant credentials needed");

                    return convertToResponse(updatedAttendance);
                } else {
                    // For testing: Allow re-entry by creating new attendance record
                    System.out.println("Employee already has attendance for today, but allowing re-entry for testing");
                    // Continue to create new attendance record below
                }
            }

            // Calculate confidence score
            double confidenceScore = calculateConfidenceScore(employee.getFaceEncoding(), base64Image);

            // Determine attendance status based on time
            Attendance.AttendanceStatus status = determineAttendanceStatus(LocalTime.now());

            // Create new attendance record
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setTenantId(employee.getTenantId()); // Set tenantId from employee
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setAttendanceDate(today);
            attendance.setStatus(status);
            attendance.setConfidenceScore(confidenceScore);

            Attendance savedAttendance = attendanceRepository.save(attendance);

            // Call external API for check-in using tenant credentials
            // Note: For now, skipping external API call as we need tenant credentials
            // TODO: Pass tenant credentials from request or employee data
            System.out.println("External API call skipped - tenant credentials needed");

            return convertToResponse(savedAttendance);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process attendance: " + e.getMessage(), e);
        }
    }

    /**
     * Record attendance directly without face recognition (for local recognition success)
     */
    public AttendanceResponse recordAttendanceDirectly(Map<String, Object> request) {
        try {
            System.out.println("=== Processing Direct Attendance ===");

            String employeeId = (String) request.get("employeeId");
            String employeeName = (String) request.get("employeeName");
            String punchDate = (String) request.get("punchDate");
            String punchTime = (String) request.get("punchTime");
            String location = (String) request.get("location");
            Double confidence = request.get("confidence") != null ?
                ((Number) request.get("confidence")).doubleValue() : 95.0;

            // Find employee by external ID
            Employee employee = employeeRepository.findByExternalId(employeeId);

            if (employee == null) {
                throw new RuntimeException("Employee not found with ID: " + employeeId);
            }
            LocalDate today = LocalDate.parse(punchDate);

            // Check if attendance already recorded for today
            List<Attendance> existingAttendances = attendanceRepository
                    .findByEmployeeAndAttendanceDateOrderByCreatedAtDesc(employee, today);

            if (!existingAttendances.isEmpty()) {
                Attendance latestAttendance = existingAttendances.get(0);

                if (latestAttendance.getCheckOutTime() == null) {
                    // Update check-out time
                    latestAttendance.setCheckOutTime(LocalDateTime.now());
                    Attendance updatedAttendance = attendanceRepository.save(latestAttendance);

                    // Call external API for check-out using tenant credentials
                    // Note: For now, skipping external API call as we need tenant credentials
                    // TODO: Pass tenant credentials from request or employee data
                    System.out.println("🔄 External MRR API call skipped - tenant credentials needed");
                    System.out.println("⚠️ Check-out saved locally but external sync skipped");

                    return convertToResponse(updatedAttendance);
                } else {
                    // Allow re-entry for testing
                    System.out.println("Employee already has attendance for today, but allowing re-entry for testing");
                }
            }

            // Create new attendance record
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setTenantId(employee.getTenantId()); // Set tenantId from employee
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setAttendanceDate(today);
            attendance.setStatus(Attendance.AttendanceStatus.PRESENT);
            attendance.setConfidenceScore(confidence);

            Attendance savedAttendance = attendanceRepository.save(attendance);

            // Call external API for check-in using tenant credentials
            // Note: For now, skipping external API call as we need tenant credentials
            // TODO: Pass tenant credentials from request or employee data
            System.out.println("🔄 External MRR API call skipped - tenant credentials needed");
            System.out.println("⚠️ Attendance saved locally but external sync skipped");

            System.out.println("✅ Direct attendance recorded successfully for: " + employeeName);
            return convertToResponse(savedAttendance);

        } catch (Exception e) {
            System.out.println("❌ Failed to process direct attendance: " + e.getMessage());
            throw new RuntimeException("Failed to process direct attendance: " + e.getMessage(), e);
        }
    }

    /**
     * Get attendance history for an employee
     */
    public List<AttendanceResponse> getAttendanceHistory(String employeeId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendances;

        if (startDate != null && endDate != null) {
            attendances = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        } else {
            Employee employee = new Employee();
            employee.setId(employeeId);
            attendances = attendanceRepository.findByEmployeeOrderByAttendanceDateDesc(employee);
        }

        return attendances.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all attendance history (for Flutter app) - tenant-specific
     */
    public List<AttendanceResponse> getAllAttendanceHistory(String employeeId, String tenantId, LocalDate startDate, LocalDate endDate) {
        // Use provided tenantId or get from current context
        String actualTenantId = (tenantId != null && !tenantId.isEmpty()) ? tenantId : getCurrentTenantId();

        List<Attendance> attendances;

        if (employeeId != null) {
            // Get history for specific employee with tenantId
            if (startDate != null && endDate != null) {
                attendances = attendanceRepository.findByTenantIdAndEmployeeIdAndDateRange(
                        actualTenantId, employeeId, startDate, endDate);
            } else {
                // For now, get all attendance for this tenant and filter by employeeId
                attendances = attendanceRepository.findAllByTenantIdOrderByAttendanceDateDesc(actualTenantId)
                        .stream()
                        .filter(attendance -> attendance.getEmployee() != null &&
                                employeeId.equals(attendance.getEmployee().getId()))
                        .collect(Collectors.toList());
            }
        } else {
            // Get all attendance history for this tenant
            if (startDate != null && endDate != null) {
                attendances = attendanceRepository.findByTenantIdAndAttendanceDateBetween(actualTenantId, startDate, endDate);
            } else {
                attendances = attendanceRepository.findAllByTenantIdOrderByAttendanceDateDesc(actualTenantId);
            }
        }

        return attendances.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get current tenant ID from context or use default
     */
    private String getCurrentTenantId() {
        // For now, return default tenant ID
        // TODO: Get from security context or request parameter
        return "12345"; // Default tenant ID
    }

    /**
     * Get daily attendance report
     */
    public List<AttendanceResponse> getDailyReport(LocalDate date) {
        List<Attendance> attendances = attendanceRepository.findByAttendanceDate(date);
        return attendances.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get today's attendance for all employees
     */
    public List<AttendanceResponse> getTodayAttendance() {
        LocalDate today = LocalDate.now();
        List<Attendance> attendances = attendanceRepository.findTodayAttendance(today);

        return attendances.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get attendance statistics for an employee
     */
    public AttendanceStats getAttendanceStats(String employeeId, LocalDate startDate, LocalDate endDate) {
        Long totalDays = attendanceRepository.countAttendanceByEmployeeAndDateRange(employeeId, startDate, endDate);

        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);

        long presentDays = attendances.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT)
                .count();

        long lateDays = attendances.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.LATE)
                .count();

        long halfDays = attendances.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.HALF_DAY)
                .count();

        return new AttendanceStats(totalDays, presentDays, lateDays, halfDays);
    }

    /**
     * Calculate confidence score for face recognition
     */
    private double calculateConfidenceScore(String knownEncoding, String testImage) {
        try {
            String testEncoding = faceRecognitionService.extractFaceEncoding(testImage);
            return faceRecognitionService.compareFaces(knownEncoding, testEncoding);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Determine attendance status based on check-in time
     */
    private Attendance.AttendanceStatus determineAttendanceStatus(LocalTime checkInTime) {
        LocalTime standardTime = LocalTime.of(9, 0); // 9:00 AM
        LocalTime lateThreshold = LocalTime.of(9, 30); // 9:30 AM
        LocalTime halfDayThreshold = LocalTime.of(12, 0); // 12:00 PM

        if (checkInTime.isBefore(lateThreshold)) {
            return Attendance.AttendanceStatus.PRESENT;
        } else if (checkInTime.isBefore(halfDayThreshold)) {
            return Attendance.AttendanceStatus.LATE;
        } else {
            return Attendance.AttendanceStatus.HALF_DAY;
        }
    }

    /**
     * Convert Attendance entity to AttendanceResponse DTO
     */
    private AttendanceResponse convertToResponse(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(attendance.getId());
        response.setEmployeeId(attendance.getEmployee().getId());
        response.setEmployeeName(attendance.getEmployee().getName());
        response.setCheckInTime(attendance.getCheckInTime());
        response.setCheckOutTime(attendance.getCheckOutTime());
        response.setAttendanceDate(attendance.getAttendanceDate());
        response.setStatus(attendance.getStatus().toString());
        response.setConfidenceScore(attendance.getConfidenceScore());
        response.setNotes(attendance.getNotes());
        response.setCreatedAt(attendance.getCreatedAt());
        return response;
    }

    /**
     * Attendance statistics class
     */
    public static class AttendanceStats {
        private final Long totalDays;
        private final Long presentDays;
        private final Long lateDays;
        private final Long halfDays;

        public AttendanceStats(Long totalDays, Long presentDays, Long lateDays, Long halfDays) {
            this.totalDays = totalDays;
            this.presentDays = presentDays;
            this.lateDays = lateDays;
            this.halfDays = halfDays;
        }

        public Long getTotalDays() { return totalDays; }
        public Long getPresentDays() { return presentDays; }
        public Long getLateDays() { return lateDays; }
        public Long getHalfDays() { return halfDays; }
        public double getAttendancePercentage() {
            return totalDays > 0 ? (presentDays * 100.0) / totalDays : 0.0;
        }
    }
}
