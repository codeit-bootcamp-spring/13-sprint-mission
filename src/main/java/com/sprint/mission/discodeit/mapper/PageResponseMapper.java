package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

    // 수정: Slice와 Page를 모두 처리하는 제네릭 매핑 메서드
    public <T> PageResponse<T> toDto(Slice<T> source) {
        Long totalElements = source instanceof Page<?> page
                ? page.getTotalElements()
                : null;

        return new PageResponse<>(
                source.getContent(),
                source.getNumber(),
                source.getSize(),
                totalElements
        );
    }
}