package com.lotus.lotusSPM.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * DTO for pagination requests.
 * Provides cursor-based and offset-based pagination support.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Min(0)
    @Builder.Default
    private int page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "ASC";

    /**
     * Convert to Spring Data Pageable
     */
    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return org.springframework.data.domain.PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /**
     * Create default page request
     */
    public static PageRequest defaultRequest() {
        return PageRequest.builder().build();
    }

    /**
     * Create page request with custom size
     */
    public static PageRequest of(int page, int size) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Create page request with sorting
     */
    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
