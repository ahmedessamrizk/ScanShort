package com.demo.utils;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T, R> PaginatedResponse<R> from(
            Page<T> page,
            Function<T, R> mapper
    ) {
        return PaginatedResponse.<R>builder()
                .content(page.map(mapper).getContent())
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}