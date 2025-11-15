package com.lotus.lotusSPM.analytics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Repository Implementation with real SQL queries
 * Provides optimized data aggregation for business intelligence
 */
@Repository
@Slf4j
public class AnalyticsRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public long countTotalStudents() {
        String sql = "SELECT COUNT(*) FROM student WHERE active = true";
        Query query = entityManager.createNativeQuery(sql);
        return ((BigInteger) query.getSingleResult()).longValue();
    }

    public Map<String, Long> countStudentsByFaculty() {
        String sql = "SELECT faculty, COUNT(*) as count FROM student " +
                "WHERE active = true AND faculty IS NOT NULL " +
                "GROUP BY faculty ORDER BY count DESC";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        Map<String, Long> facultyCount = new LinkedHashMap<>();
        for (Object[] row : results) {
            String faculty = (String) row[0];
            Long count = ((BigInteger) row[1]).longValue();
            facultyCount.put(faculty, count);
        }

        return facultyCount;
    }

    public Map<String, Long> countStudentsByDepartment() {
        String sql = "SELECT department, COUNT(*) as count FROM student " +
                "WHERE active = true AND department IS NOT NULL " +
                "GROUP BY department ORDER BY count DESC LIMIT 20";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        Map<String, Long> departmentCount = new LinkedHashMap<>();
        for (Object[] row : results) {
            String department = (String) row[0];
            Long count = ((BigInteger) row[1]).longValue();
            departmentCount.put(department, count);
        }

        return departmentCount;
    }

    public Map<String, Long> countByInternshipStatus() {
        String sql = "SELECT internship_status, COUNT(*) as count FROM student " +
                "WHERE active = true AND internship_status IS NOT NULL " +
                "GROUP BY internship_status";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        Map<String, Long> statusCount = new LinkedHashMap<>();
        for (Object[] row : results) {
            String status = (String) row[0];
            Long count = ((BigInteger) row[1]).longValue();
            statusCount.put(status, count);
        }

        return statusCount;
    }

    public long countPlacedStudents() {
        String sql = "SELECT COUNT(DISTINCT s.id) FROM student s " +
                "INNER JOIN application_form af ON s.id = af.student_id " +
                "WHERE s.active = true AND af.status = 'ACCEPTED'";

        Query query = entityManager.createNativeQuery(sql);
        Object result = query.getSingleResult();
        return result != null ? ((BigInteger) result).longValue() : 0L;
    }

    public List<Map<String, Object>> getTopRecruitingCompanies(int limit) {
        String sql = "SELECT o.company_name, COUNT(DISTINCT af.id) as placements " +
                "FROM opportunities o " +
                "INNER JOIN application_form af ON o.id = af.opportunity_id " +
                "WHERE af.status = 'ACCEPTED' " +
                "GROUP BY o.company_name " +
                "ORDER BY placements DESC " +
                "LIMIT :limit";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> companies = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> company = new HashMap<>();
            company.put("companyName", row[0]);
            company.put("placements", ((BigInteger) row[1]).longValue());
            companies.add(company);
        }

        return companies;
    }

    public List<Map<String, Object>> getEnrollmentTrend(LocalDateTime startDate) {
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count " +
                "FROM student " +
                "WHERE created_at >= :startDate " +
                "GROUP BY month " +
                "ORDER BY month ASC";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("month", row[0]);
            dataPoint.put("count", ((BigInteger) row[1]).longValue());
            trend.add(dataPoint);
        }

        return trend;
    }

    public List<Map<String, Object>> getFilteredData(String faculty, String department,
                                                      String dateFrom, String dateTo) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s.id, s.name, s.surname, s.email, s.faculty, s.department, ");
        sql.append("s.internship_status, s.created_at ");
        sql.append("FROM student s WHERE s.active = true");

        Map<String, Object> params = new HashMap<>();

        if (faculty != null && !faculty.isEmpty()) {
            sql.append(" AND s.faculty = :faculty");
            params.put("faculty", faculty);
        }

        if (department != null && !department.isEmpty()) {
            sql.append(" AND s.department = :department");
            params.put("department", department);
        }

        if (dateFrom != null && !dateFrom.isEmpty()) {
            sql.append(" AND s.created_at >= :dateFrom");
            params.put("dateFrom", LocalDateTime.parse(dateFrom + "T00:00:00"));
        }

        if (dateTo != null && !dateTo.isEmpty()) {
            sql.append(" AND s.created_at <= :dateTo");
            params.put("dateTo", LocalDateTime.parse(dateTo + "T23:59:59"));
        }

        sql.append(" LIMIT 1000");

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        List<Object[]> results = query.getResultList();

        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", ((BigInteger) row[0]).longValue());
            record.put("name", row[1]);
            record.put("surname", row[2]);
            record.put("email", row[3]);
            record.put("faculty", row[4]);
            record.put("department", row[5]);
            record.put("internshipStatus", row[6]);
            record.put("createdAt", row[7]);
            data.add(record);
        }

        return data;
    }

    public Map<String, Object> getStudentData(Long studentId) {
        String sql = "SELECT s.id, s.name, s.surname, s.email, s.faculty, s.department, " +
                "s.internship_status, COUNT(af.id) as applications, " +
                "SUM(CASE WHEN af.status = 'ACCEPTED' THEN 1 ELSE 0 END) as acceptances " +
                "FROM student s " +
                "LEFT JOIN application_form af ON s.id = af.student_id " +
                "WHERE s.id = :studentId " +
                "GROUP BY s.id, s.name, s.surname, s.email, s.faculty, s.department, s.internship_status";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("studentId", studentId);

        try {
            Object[] row = (Object[]) query.getSingleResult();

            Map<String, Object> data = new HashMap<>();
            data.put("id", ((BigInteger) row[0]).longValue());
            data.put("name", row[1]);
            data.put("surname", row[2]);
            data.put("email", row[3]);
            data.put("faculty", row[4]);
            data.put("department", row[5]);
            data.put("internshipStatus", row[6]);
            data.put("applications", ((BigDecimal) row[7]).intValue());
            data.put("acceptances", ((BigDecimal) row[8]).intValue());

            return data;
        } catch (Exception ex) {
            log.error("Error fetching student data for id: {}", studentId, ex);
            return new HashMap<>();
        }
    }

    /**
     * Get average applications per student by department
     */
    public Map<String, Object> getApplicationStatistics() {
        String sql = "SELECT s.department, " +
                "COUNT(DISTINCT s.id) as students, " +
                "COUNT(af.id) as total_applications, " +
                "ROUND(COUNT(af.id) / COUNT(DISTINCT s.id), 2) as avg_per_student " +
                "FROM student s " +
                "LEFT JOIN application_form af ON s.id = af.student_id " +
                "WHERE s.active = true AND s.department IS NOT NULL " +
                "GROUP BY s.department " +
                "ORDER BY avg_per_student DESC " +
                "LIMIT 10";

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> byDepartment = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> deptStat = new HashMap<>();
            deptStat.put("department", row[0]);
            deptStat.put("students", ((BigInteger) row[1]).longValue());
            deptStat.put("totalApplications", ((BigInteger) row[2]).longValue());
            deptStat.put("avgPerStudent", row[3] != null ? ((BigDecimal) row[3]).doubleValue() : 0.0);
            byDepartment.add(deptStat);
        }

        stats.put("byDepartment", byDepartment);
        return stats;
    }

    /**
     * Get opportunity statistics
     */
    public Map<String, Object> getOpportunityStatistics() {
        String sql = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN deadline >= CURRENT_DATE THEN 1 ELSE 0 END) as active, " +
                "SUM(CASE WHEN deadline < CURRENT_DATE THEN 1 ELSE 0 END) as expired " +
                "FROM opportunities";

        Query query = entityManager.createNativeQuery(sql);
        Object[] row = (Object[]) query.getSingleResult();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", ((BigInteger) row[0]).longValue());
        stats.put("active", row[1] != null ? ((BigDecimal) row[1]).longValue() : 0L);
        stats.put("expired", row[2] != null ? ((BigDecimal) row[2]).longValue() : 0L);

        return stats;
    }
}
