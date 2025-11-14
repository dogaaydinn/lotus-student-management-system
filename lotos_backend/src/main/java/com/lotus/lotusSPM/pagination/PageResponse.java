package com.lotus.lotusSPM.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic Page Response wrapper.
 *
 * Enterprise Pattern: Standardized API Response
 *
 * Provides:
 * - Content list
 * - Pagination metadata
 * - Navigation information
 * - Total counts
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private PageMetadata metadata;

    /**
     * Create from Spring Data Page.
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageMetadata metadata = new PageMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new PageResponse<>(page.getContent(), metadata);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageMetadata {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
