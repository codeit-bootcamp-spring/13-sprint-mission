package com.sprint.mission.discodeit.dto.response;

import io.swagger.v3.oas.annotations.media.*;

import java.util.*;


@Schema(description = "공통 페이지 응답")
public record PageResponse<T>(
        @Schema(description = "조회된 데이터 목록")
        List<T> content,

        @Schema(description = "다음 페이지를 조회할 때 사용할 커서")
        Object nextCursor,

        @Schema(description = "페이지당 데이터 개수")
        int size,

        @Schema(description = "전체 데이터 개수")
        Long totalElements,

        @Schema(description = "다음 페이지 존재 여부")
        boolean hasNext
) {
    public static <T> PageResponse<T> of(
            List<T> content,
            Object nextCursor,
            int size,
            Long totalElements,
            boolean hasNext
    ) {
        return new PageResponse<>(
                content,
                nextCursor,
                size,
                totalElements,
                hasNext
        );
    }
}


