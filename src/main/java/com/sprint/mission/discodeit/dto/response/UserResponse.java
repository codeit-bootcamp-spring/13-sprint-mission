package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public record UserResponse(
        UUID id,
        String userName,
        String email,
        boolean online,
        ProfileResponse profile
        ) {

    public record ProfileResponse(
            UUID id,
            String fileName,
            String contentType
    ) {

        public static ProfileResponse from(BinaryContent profile) {
            return new ProfileResponse(
                    profile.getId(),
                    profile.getFileName(),
                    profile.getContentType()
            );
        }
    }

    public static UserResponse from(User user, UserStatus userStatus, BinaryContent profile) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                userStatus != null && userStatus.isOnline(),
                profile == null ? null : ProfileResponse.from(profile)
        );
    }

    @Override
    public String toString() {
        return """
                User
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
                        userName,
                        email,
                        online,
                        profile == null ? "없음" : profile.fileName()
                );
    }


}
