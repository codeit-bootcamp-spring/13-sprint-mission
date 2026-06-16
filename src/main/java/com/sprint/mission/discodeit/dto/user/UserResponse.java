package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.UUID;

public record UserResponse(
        // 패스워드 정보 제외, 온라인 상태 정보 포함
        UUID id,
        String username,
        String email,
        UUID profileId,
        UserStatus userStatus
) {
    public static UserResponse from(User user, UserStatus userStatus) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                userStatus
        );
    }
}
