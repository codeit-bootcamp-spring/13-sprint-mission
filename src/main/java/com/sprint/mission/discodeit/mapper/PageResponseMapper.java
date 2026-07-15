package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import org.springframework.data.domain.*;

public class PageResponseMapper {

    public static <T> PageResponse<T> fromSlice(Slice<T> slice) {
        return new PageResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                null,
                slice.hasNext()
        );
    }

    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.hasNext()
        );
    }
}
