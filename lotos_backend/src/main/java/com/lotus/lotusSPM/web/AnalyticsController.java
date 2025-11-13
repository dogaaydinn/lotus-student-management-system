package com.lotus.lotusSPM.web;

import com.lotus.lotusSPM.analytics.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Business Intelligence and Analytics APIs")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/enrollment")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Get enrollment statistics", description = "Retrieve enrollment data by faculty, department, and status")
    public ResponseEntity<?> getEnrollmentStatistics() {
        try {
            Map<String, Object> stats = analyticsService.getEnrollmentStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving enrollment statistics: " + e.getMessage());
        }
    }

    @GetMapping("/placement")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR', 'CAREER_CENTER')")
    @Operation(summary = "Get placement statistics", description = "Retrieve placement rates and top companies")
    public ResponseEntity<?> getPlacementStatistics() {
        try {
            Map<String, Object> stats = analyticsService.getPlacementStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving placement statistics: " + e.getMessage());
        }
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Get enrollment trend", description = "Retrieve time-series enrollment data")
    public ResponseEntity<?> getEnrollmentTrend(@RequestParam(defaultValue = "12") int months) {
        try {
            List<Map<String, Object>> trend = analyticsService.getEnrollmentTrend(months);
            return ResponseEntity.ok(trend);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving trend data: " + e.getMessage());
        }
    }

    @PostMapping("/report/custom")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate custom report", description = "Create custom analytics report with filters")
    public ResponseEntity<?> generateCustomReport(@RequestBody Map<String, String> filters) {
        try {
            Map<String, Object> report = analyticsService.generateCustomReport(filters);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating report: " + e.getMessage());
        }
    }

    @GetMapping("/predict/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Predict student success", description = "Get ML-based success prediction for a student")
    public ResponseEntity<?> predictStudentSuccess(@PathVariable Long studentId) {
        try {
            Map<String, Object> prediction = analyticsService.predictStudentSuccess(studentId);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating prediction: " + e.getMessage());
        }
    }
}
