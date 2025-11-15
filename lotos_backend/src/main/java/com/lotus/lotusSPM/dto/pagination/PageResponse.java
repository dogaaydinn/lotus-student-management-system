package com.lotus.lotusSPM.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * Contains page metadata along with the actual content.
 *
 * @param <T> Type of content in the page
 * @author Lotus SMS Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    /**
     * Create PageResponse from Spring Data Page
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Check if there are more pages
     */
    public boolean hasNext() {
        return !last;
    }

    /**
     * Check if there are previous pages
     */
    public boolean hasPrevious() {
        return !first;
    }

    /**
     * Get next page number
     */
    public int getNextPage() {
        return hasNext() ? page + 1 : page;
    }

    /**
     * Get previous page number
     */
    public int getPreviousPage() {
        return hasPrevious() ? page - 1 : 0;
    }
}
