package com.lotus.lotusSPM.util;

import com.lotus.lotusSPM.dto.pagination.PageRequest;
import com.lotus.lotusSPM.dto.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Base64;
import java.util.function.Function;

/**
 * Utility class for pagination operations.
 * Provides helpers for creating page requests, responses, and cursors.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
public class PaginationUtil {

    private PaginationUtil() {
        // Utility class
    }

    /**
     * Create default pageable with standard page size
     */
    public static Pageable defaultPageable() {
        return org.springframework.data.domain.PageRequest.of(0, 20, Sort.by("id").descending());
    }

    /**
     * Create pageable from page and size
     */
    public static Pageable createPageable(int page, int size) {
        return org.springframework.data.domain.PageRequest.of(page, Math.min(size, 100));
    }

    /**
     * Create pageable with sorting
     */
    public static Pageable createPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        return org.springframework.data.domain.PageRequest.of(
                page,
                Math.min(size, 100),
                Sort.by(sortDirection, sortBy)
        );
    }

    /**
     * Convert Page to PageResponse with mapping function
     */
    public static <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        Page<R> mappedPage = page.map(mapper);
        return PageResponse.of(mappedPage);
    }

    /**
     * Encode cursor for cursor-based pagination
     */
    public static String encodeCursor(Long id) {
        if (id == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(id.toString().getBytes());
    }

    /**
     * Decode cursor for cursor-based pagination
     */
    public static Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Long.parseLong(decoded);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculate total pages
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Validate page parameters
     */
    public static void validatePageParameters(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be >= 1");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Page size must be <= 100");
        }
    }
}
