package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PageResponseMapper {

    public <T, R> PageResponse<R> fromSlice(
            Slice<T> slice,
            Function<T, R> mapper
    ) {
        return new PageResponse<>(
                slice.getContent().stream()
                        .map(mapper)
                        .toList(),
                slice.getNumber(),
                slice.getSize(),
                null
        );
    }

    public <T, R> PageResponse<R> fromPage(
            Page<T> page,
            Function<T, R> mapper
    ) {
        return new PageResponse<>(
                page.getContent().stream()
                        .map(mapper)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}