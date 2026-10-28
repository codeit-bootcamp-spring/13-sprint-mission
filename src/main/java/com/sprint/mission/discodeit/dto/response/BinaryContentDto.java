package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.aspect.LoggableResult;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 파일 메타데이터와 업로드 상태를 함께 전달하는 응답이다.
 * HTTP 응답과 binaryContents.updated 이벤트가 같은 구조를 사용하여 클라이언트가 상태를 갱신한다.
 */
public record BinaryContentDto(
        UUID id,
        String fileName,
        Long size,
        String contentType,
        BinaryContentStatus status
) implements LoggableResult {


    @Override
    public Map<String, Object> logFields() {
        Map<String, Object> logFields = new LinkedHashMap<>();
        addLogFields(logFields, "binaryContentId", id);
        return logFields;
    }

    private void addLogFields(Map<String, Object> logFields, String key, Object value) {
        logFields.put(key, value);
    }
}