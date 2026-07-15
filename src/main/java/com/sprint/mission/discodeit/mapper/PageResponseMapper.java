package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

    // Slice: 다음 페이지 유무만 알고 전체 개수는 모름
    public <T> PageResponse<T> fromSlice(Slice<T> slice) {
        return new PageResponse<>(
                slice.getContent(),
                slice.getSize(),
                slice.hasNext(),
                null             // Slice는 전체 개수를 조회하지 않아 null
        );
    }

    // Page: 전체 개수까지 알고 있음
    public <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getSize(),
                page.hasNext(),
                page.getTotalElements()
        );
    }
}