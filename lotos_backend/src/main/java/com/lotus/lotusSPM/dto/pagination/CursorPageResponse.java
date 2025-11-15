package com.lotus.lotusSPM.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Cursor-based pagination response for infinite scroll scenarios.
 * Better performance for large datasets compared to offset pagination.
 *
 * @param <T> Type of content
 * @author Lotus SMS Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {

    private List<T> content;
    private String nextCursor;
    private String previousCursor;
    private boolean hasNext;
    private boolean hasPrevious;
    private int size;

    /**
     * Create cursor response with next cursor only
     */
    public static <T> CursorPageResponse<T> of(List<T> content, String nextCursor, int size) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(nextCursor != null)
                .hasPrevious(false)
                .size(size)
                .build();
    }

    /**
     * Create cursor response with both cursors
     */
    public static <T> CursorPageResponse<T> of(List<T> content, String nextCursor, String previousCursor, int size) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .previousCursor(previousCursor)
                .hasNext(nextCursor != null)
                .hasPrevious(previousCursor != null)
                .size(size)
                .build();
    }
}
