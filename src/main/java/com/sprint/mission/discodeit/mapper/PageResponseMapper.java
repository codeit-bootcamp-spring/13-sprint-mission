package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import org.springframework.data.domain.*;

public class PageResponseMapper {

    private PageResponseMapper() {
    }

    public static <T> PageResponse<T> fromSlice(Slice<T> slice, Object nextCursor) {
        return new PageResponse<>(
                slice.getContent(),
                nextCursor,
                slice.getSize(),
                null,
                slice.hasNext()
        );
    }

    public static <T> PageResponse<T> fromPage(Page<T> page, Object nextCursor) {
        return new PageResponse<>(
                page.getContent(),
                nextCursor,
                page.getSize(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}
