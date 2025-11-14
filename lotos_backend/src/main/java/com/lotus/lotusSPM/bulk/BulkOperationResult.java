package com.lotus.lotusSPM.bulk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Result of a bulk operation.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkOperationResult {
    private int successCount;
    private int failureCount;
    private List<String> errors;

    public int getTotalCount() {
        return successCount + failureCount;
    }

    public boolean isFullSuccess() {
        return failureCount == 0;
    }

    public boolean isPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }

    public boolean isFullFailure() {
        return successCount == 0 && failureCount > 0;
    }
}
