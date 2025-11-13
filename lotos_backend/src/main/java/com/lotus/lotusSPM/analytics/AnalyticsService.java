package com.lotus.lotusSPM.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service for Phase 2: Advanced Analytics
 * Provides business intelligence and reporting capabilities
 */
@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    /**
     * Get student enrollment statistics
     */
    public Map<String, Object> getEnrollmentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Total students
        long totalStudents = analyticsRepository.countTotalStudents();
        stats.put("totalStudents", totalStudents);

        // Students by faculty
        Map<String, Long> byFaculty = analyticsRepository.countStudentsByFaculty();
        stats.put("byFaculty", byFaculty);

        // Students by department
        Map<String, Long> byDepartment = analyticsRepository.countStudentsByDepartment();
        stats.put("byDepartment", byDepartment);

        // Internship status distribution
        Map<String, Long> byInternshipStatus = analyticsRepository.countByInternshipStatus();
        stats.put("byInternshipStatus", byInternshipStatus);

        return stats;
    }

    /**
     * Get placement success rate
     */
    public Map<String, Object> getPlacementStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalStudents = analyticsRepository.countTotalStudents();
        long placedStudents = analyticsRepository.countPlacedStudents();

        double placementRate = totalStudents > 0
            ? (double) placedStudents / totalStudents * 100
            : 0.0;

        stats.put("totalStudents", totalStudents);
        stats.put("placedStudents", placedStudents);
        stats.put("placementRate", placementRate);
        stats.put("topCompanies", analyticsRepository.getTopRecruitingCompanies(10));

        return stats;
    }

    /**
     * Get time-series data for dashboard charts
     */
    public List<Map<String, Object>> getEnrollmentTrend(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        return analyticsRepository.getEnrollmentTrend(startDate);
    }

    /**
     * Generate custom report based on filters
     */
    public Map<String, Object> generateCustomReport(Map<String, String> filters) {
        Map<String, Object> report = new HashMap<>();

        // Apply filters and aggregate data
        String faculty = filters.get("faculty");
        String department = filters.get("department");
        String dateFrom = filters.get("dateFrom");
        String dateTo = filters.get("dateTo");

        // Execute filtered query
        List<Map<String, Object>> data = analyticsRepository.getFilteredData(
            faculty, department, dateFrom, dateTo
        );

        report.put("data", data);
        report.put("summary", calculateSummary(data));
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    /**
     * Predict student success using ML model placeholder
     */
    public Map<String, Object> predictStudentSuccess(Long studentId) {
        // Placeholder for ML model integration (Phase 3)
        Map<String, Object> prediction = new HashMap<>();

        // Get student historical data
        Map<String, Object> studentData = analyticsRepository.getStudentData(studentId);

        // Mock prediction (to be replaced with actual ML model)
        double successProbability = Math.random(); // Replace with model prediction

        prediction.put("studentId", studentId);
        prediction.put("successProbability", successProbability);
        prediction.put("riskLevel", successProbability < 0.5 ? "HIGH" : successProbability < 0.75 ? "MEDIUM" : "LOW");
        prediction.put("recommendations", generateRecommendations(successProbability));

        return prediction;
    }

    private Map<String, Object> calculateSummary(List<Map<String, Object>> data) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRecords", data.size());
        summary.put("calculatedAt", LocalDateTime.now());
        return summary;
    }

    private List<String> generateRecommendations(double successProbability) {
        List<String> recommendations = new ArrayList<>();

        if (successProbability < 0.5) {
            recommendations.add("Schedule academic counseling session");
            recommendations.add("Enroll in tutoring program");
            recommendations.add("Monitor attendance closely");
        } else if (successProbability < 0.75) {
            recommendations.add("Provide career guidance");
            recommendations.add("Encourage internship applications");
        } else {
            recommendations.add("Recommend for leadership programs");
            recommendations.add("Consider for merit scholarships");
        }

        return recommendations;
    }
}
