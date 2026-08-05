package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final BinaryContentMapper binaryContentMapper;

    public UserResponse toDto(User user) {
        if (user == null) {
            return null;
        }

        Boolean online = user.getStatus() != null && user.getStatus().isOnline();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                online,
                binaryContentMapper.toDto(user.getProfile())
        );
    }
}