package com.lotus.lotusSPM.bulk;

import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.pagination.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Bulk Operations Controller for batch processing.
 *
 * Enterprise Pattern: Batch Processing / Bulk Operations
 *
 * Features:
 * - Bulk create (batch insert)
 * - Bulk update (batch update)
 * - Bulk delete (batch delete)
 * - CSV import/export
 * - Excel import/export
 * - Async processing for large datasets
 * - Progress tracking
 * - Error reporting
 *
 * Benefits:
 * - Reduced network overhead
 * - Database optimization (batching)
 * - Better throughput
 * - Transaction management
 * - Rollback on error
 */
@RestController
@RequestMapping("/api/v1/bulk")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bulk Operations", description = "Batch processing endpoints")
public class BulkOperationsController {

    private final BulkOperationsService bulkOperationsService;

    /**
     * Bulk create students.
     */
    @PostMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk create students", description = "Create multiple students in a single transaction")
    public ResponseEntity<BulkOperationResult> bulkCreateStudents(
            @Valid @RequestBody List<Student> students) {

        log.info("Bulk creating {} students", students.size());

        BulkOperationResult result = bulkOperationsService.bulkCreateStudents(students);

        return ResponseEntity.ok(result);
    }

    /**
     * Bulk update students.
     */
    @PutMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk update students", description = "Update multiple students in a single transaction")
    public ResponseEntity<BulkOperationResult> bulkUpdateStudents(
            @Valid @RequestBody List<Student> students) {

        log.info("Bulk updating {} students", students.size());

        BulkOperationResult result = bulkOperationsService.bulkUpdateStudents(students);

        return ResponseEntity.ok(result);
    }

    /**
     * Bulk delete students by IDs.
     */
    @DeleteMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk delete students", description = "Delete multiple students by IDs")
    public ResponseEntity<BulkOperationResult> bulkDeleteStudents(
            @RequestBody List<Long> studentIds) {

        log.info("Bulk deleting {} students", studentIds.size());

        BulkOperationResult result = bulkOperationsService.bulkDeleteStudents(studentIds);

        return ResponseEntity.ok(result);
    }

    /**
     * Import students from CSV file.
     */
    @PostMapping("/students/import/csv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import students from CSV", description = "Import multiple students from CSV file")
    public ResponseEntity<BulkOperationResult> importStudentsFromCsv(
            @RequestParam("file") MultipartFile file) {

        log.info("Importing students from CSV file: {}", file.getOriginalFilename());

        BulkOperationResult result = bulkOperationsService.importStudentsFromCsv(file);

        return ResponseEntity.ok(result);
    }

    /**
     * Import students from Excel file.
     */
    @PostMapping("/students/import/excel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Import students from Excel", description = "Import multiple students from Excel file")
    public ResponseEntity<BulkOperationResult> importStudentsFromExcel(
            @RequestParam("file") MultipartFile file) {

        log.info("Importing students from Excel file: {}", file.getOriginalFilename());

        BulkOperationResult result = bulkOperationsService.importStudentsFromExcel(file);

        return ResponseEntity.ok(result);
    }

    /**
     * Export students to CSV.
     */
    @GetMapping("/students/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Export students to CSV", description = "Export all students to CSV file")
    public ResponseEntity<byte[]> exportStudentsToCsv() {

        log.info("Exporting students to CSV");

        byte[] csvData = bulkOperationsService.exportStudentsToCsv();

        return ResponseEntity.ok()
            .header("Content-Type", "text/csv")
            .header("Content-Disposition", "attachment; filename=students.csv")
            .body(csvData);
    }

    /**
     * Export students to Excel.
     */
    @GetMapping("/students/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Export students to Excel", description = "Export all students to Excel file")
    public ResponseEntity<byte[]> exportStudentsToExcel() {

        log.info("Exporting students to Excel");

        byte[] excelData = bulkOperationsService.exportStudentsToExcel();

        return ResponseEntity.ok()
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Content-Disposition", "attachment; filename=students.xlsx")
            .body(excelData);
    }

    /**
     * Get bulk operation status.
     */
    @GetMapping("/operations/{operationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Get bulk operation status", description = "Check the status of an async bulk operation")
    public ResponseEntity<BulkOperationStatus> getOperationStatus(
            @PathVariable String operationId) {

        log.info("Getting status for bulk operation: {}", operationId);

        BulkOperationStatus status = bulkOperationsService.getOperationStatus(operationId);

        return ResponseEntity.ok(status);
    }
}
