package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String username;
    private String email;
    private boolean online;

    public static UserResponse from(
            User user,
            UserStatus status) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                status.isOnline()
        );
    }
}
