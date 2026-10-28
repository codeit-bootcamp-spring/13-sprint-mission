package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.aspect.LoggableResult;
import com.sprint.mission.discodeit.entity.Role;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @param online 등록된 세션에 유효한 Access Token이 하나라도 있는지 여부.
 *               Access Token 유효기간을 따르는 표시이며 실제 활동 여부를 의미하지 않는다.
 */
public record UserDto(
        UUID id,
        String username,
        String email,
        BinaryContentDto profile,
        boolean online,
        Role role,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) implements LoggableResult {


    @Override
    public Map<String, Object> logFields() {
        Map<String, Object> logFields = new LinkedHashMap<>();
        addLogFields(logFields, "userId", id);
        if (profile != null) {
            addLogFields(logFields, "profileId", profile.id());
        }
        return logFields;
    }

    /**
     * 온라인 여부를 반전하여 만료 모니터의 오프라인 분기에서 사용한다.
     * online 값이 계산되어 null이 아닌 DTO를 대상으로 호출한다.
     */
    public boolean offline(){
        return !online;
    }

    private void addLogFields(Map<String, Object> logFields, String key, Object value) {
        logFields.put(key, value);
    }
}
