package com.faceattendance.controller;

import com.faceattendance.dto.AttendanceRequest;
import com.faceattendance.dto.AttendanceResponse;
import com.faceattendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * Record attendance using face recognition
     */
    @PostMapping("/checkin")
    public ResponseEntity<?> recordAttendance(@Valid @RequestBody AttendanceRequest request) {
        try {
            AttendanceResponse response = attendanceService.recordAttendance(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Attendance recording failed", e.getMessage()));
        }
    }

    /**
     * Mark attendance using face recognition with image upload
     */
    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@RequestParam("image") MultipartFile image) {
        try {
            AttendanceResponse response = attendanceService.recordAttendanceWithImage(image);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Attendance recording failed", e.getMessage()));
        }
    }

    /**
     * Mark attendance directly without face recognition (for local recognition success)
     */
    @PostMapping("/mark-direct")
    public ResponseEntity<?> markAttendanceDirectly(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== Direct Attendance Request ===");
            System.out.println("Employee ID: " + request.get("employeeId"));
            System.out.println("Employee Name: " + request.get("employeeName"));
            System.out.println("Punch Date: " + request.get("punchDate"));
            System.out.println("Punch Time: " + request.get("punchTime"));
            System.out.println("Location: " + request.get("location"));
            System.out.println("Confidence: " + request.get("confidence"));
            System.out.println("Source: " + request.get("source"));

            AttendanceResponse response = attendanceService.recordAttendanceDirectly(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("Direct attendance failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Direct attendance recording failed", e.getMessage()));
        }
    }

    /**
     * Get attendance history for an employee
     */
    @GetMapping("/history/{employeeId}")
    public ResponseEntity<?> getAttendanceHistory(
            @PathVariable String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceResponse> history = attendanceService.getAttendanceHistory(employeeId, startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to fetch attendance history", e.getMessage()));
        }
    }

    /**
     * Get all attendance history (for Flutter app)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getAllAttendanceHistory(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<AttendanceResponse> history = attendanceService.getAllAttendanceHistory(employeeId, startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to fetch attendance history", e.getMessage()));
        }
    }

    /**
     * Get daily attendance report
     */
    @GetMapping("/daily-report")
    public ResponseEntity<?> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AttendanceResponse> report = attendanceService.getDailyReport(date);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to fetch daily report", e.getMessage()));
        }
    }

    /**
     * Get today's attendance for all employees
     */
    @GetMapping("/today")
    public ResponseEntity<List<AttendanceResponse>> getTodayAttendance() {
        List<AttendanceResponse> todayAttendance = attendanceService.getTodayAttendance();
        return ResponseEntity.ok(todayAttendance);
    }

    /**
     * Get attendance statistics for an employee
     */
    @GetMapping("/stats/{employeeId}")
    public ResponseEntity<?> getAttendanceStats(
            @PathVariable String employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            AttendanceService.AttendanceStats stats = attendanceService.getAttendanceStats(employeeId, startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to fetch attendance statistics", e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Attendance service is running");
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
}
