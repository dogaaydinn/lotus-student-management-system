package com.lotus.lotusSPM.bulk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Status of an async bulk operation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkOperationStatus {
    private String operationId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int processedCount;
    private int totalCount;

    public double getProgress() {
        if (totalCount == 0) {
            return 0;
        }
        return (double) processedCount / totalCount * 100;
    }
}
