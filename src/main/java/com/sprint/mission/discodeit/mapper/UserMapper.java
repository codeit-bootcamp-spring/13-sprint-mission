package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private static final Duration ONLINE_THRESHOLD = Duration.ofMinutes(5);

    private final BinaryContentMapper binaryContentMapper;

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                binaryContentMapper.toDto(user.getProfile()),
                isOnline(user)
        );
    }

    private boolean isOnline(User user) {
        if (user.getStatus() == null
                || user.getStatus().getLastActiveAt() == null) {
            return false;
        }

        Instant onlineBoundary = Instant.now()
                .minus(ONLINE_THRESHOLD);

        return user.getStatus()
                .getLastActiveAt()
                .isAfter(onlineBoundary);
    }
}