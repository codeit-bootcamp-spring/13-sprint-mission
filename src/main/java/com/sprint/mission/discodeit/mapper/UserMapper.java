package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final UserStatusRepository userStatusRepository;

    public UserDto toDto(User user) {
        Boolean online = userStatusRepository.findByUser_Id(user.getId())
                .map(UserStatus::isOnline)
                .orElse(null);

        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                binaryContentMapper.toDto(user.getProfile()),
                online
        );
    }
}