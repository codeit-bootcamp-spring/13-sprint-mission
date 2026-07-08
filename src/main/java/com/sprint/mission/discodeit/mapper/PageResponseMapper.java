package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PageResponseMapper {

    public <T> PageResponse<T> toDto(Slice<T> slice, Function<T, ?> cursorExtractor) {
        Object nextCursor = slice.getContent().isEmpty() ? null
                : cursorExtractor.apply(slice.getContent().get(slice.getContent().size() - 1));


        return new PageResponse<>(
                slice.getContent(),
                slice.hasNext() ? nextCursor : null,
                slice.getSize(),
                slice.hasNext(),
                null
        );
    }


}
