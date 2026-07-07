package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final BinaryContentMapper binaryContentMapper;

    public UserDto toDto(User user, UserStatus userStatus) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Optional.ofNullable(user.getProfile()).map(binaryContentMapper::toDto).orElse(null),
                userStatus != null && userStatus.isOnline()
        );
    }

}
