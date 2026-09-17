package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.aspect.LoggableResult;
import com.sprint.mission.discodeit.entity.Role;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

    private void addLogFields(Map<String, Object> logFields, String key, Object value) {
        logFields.put(key, value);
    }
}
