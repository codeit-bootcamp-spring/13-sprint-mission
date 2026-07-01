package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public record UserResponse(
        UUID id,
        String username,
        String email,
        boolean online,
        UUID profileId
        ) {

    public static UserResponse from(User user, UserStatus userStatus, BinaryContent profile) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                userStatus != null && userStatus.isOnline(),
                profile == null ? null : profile.getId()
        );
    }

    @Override
    public String toString() {
        return """
                유저 정보
                ====================
                ID      : %s
                Name    : %s
                Email   : %s
                Online  : %s
                Profile : %s
                ====================
                """
                .formatted(
                        id,
                        username,
                        email,
                        online,
                        profileId == null ? "없음" : profileId
                );
    }


}
