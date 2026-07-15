package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PageResponseMapper {

    public <T> PageResponse<T> toCursorPageResponse(
            List<T> content,
            Object nextCursor,
            int size,
            boolean hasNext
    ) {
        return new PageResponse<>(
                content,
                nextCursor,
                size,
                hasNext,
                null
        );
    }
}