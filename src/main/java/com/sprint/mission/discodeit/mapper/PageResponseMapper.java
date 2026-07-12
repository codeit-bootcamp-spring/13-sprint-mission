package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Function;

@Component
public class PageResponseMapper {

    public <T> PageResponse<T> fromSlice(Slice<T> slice, Function<T, Instant> cursorExtractor) {
        Instant nextCursor = null;

        if (slice.hasNext() && !slice.getContent().isEmpty()) {
            T lastContent = slice.getContent().get(slice.getContent().size() - 1);
            nextCursor = cursorExtractor.apply(lastContent);
        }

        return new PageResponse<>(
                slice.getContent(),
                nextCursor,
                slice.getSize(),
                slice.hasNext(),
                null
        );
    }

    public <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                null,
                page.getSize(),
                page.hasNext(),
                page.getTotalElements()
        );
    }
}