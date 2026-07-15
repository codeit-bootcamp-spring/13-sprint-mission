package com.sprint.mission.discodeit.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,       // 실제 데이터 목록
        int size,              // 페이지 크기 (50)
        boolean hasNext,       // 다음 페이지가 있는지
        Long totalElements     // 전체 데이터 수 (null 가능)
) {

}