package com.lotus.lotusSPM.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;

/**
 * Pageable DTO for API pagination requests.
 *
 * Enterprise Pattern: Pagination / Cursor-Based Navigation
 *
 * Features:
 * - Page-based pagination
 * - Sorting support (multiple fields)
 * - Size limits
 * - Default values
 *
 * Best Practices:
 * - Limit max page size (prevent DOS)
 * - Provide total count
 * - Support cursor-based pagination for large datasets
 * - Include navigation links (HATEOAS)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableDTO {

    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must be <= 100")
    private int size = 20;

    private String sort = "id";
    private String direction = "ASC";

    /**
     * Convert to Spring Pageable.
     */
    public Pageable toPageable() {
        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(sortDirection, sort));
    }

    /**
     * Support multiple sort fields.
     */
    public Pageable toPageable(List<String> sortFields, List<String> directions) {
        List<Sort.Order> orders = new ArrayList<>();

        for (int i = 0; i < sortFields.size(); i++) {
            Sort.Direction dir = i < directions.size() && "DESC".equalsIgnoreCase(directions.get(i))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

            orders.add(new Sort.Order(dir, sortFields.get(i)));
        }

        return PageRequest.of(page, size, Sort.by(orders));
    }
}
