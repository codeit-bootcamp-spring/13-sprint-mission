package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusUpdateResponse(
        UUID userStatusId,
        Instant createdAt,
        Instant updatedAt,
        UUID userId,
        Instant lastAccessTime
) {
    public static UserStatusUpdateResponse from(UserStatus userStatus) {
        return new UserStatusUpdateResponse(
                userStatus.getId(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt(),
                userStatus.getUserId(),
                userStatus.getLastActiveAt()
        );
    }
}
