package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserStatusRepository userStatusRepository;

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
        Boolean online = userStatus != null && userStatus.isOnline();
        UUID profileId = user.getProfile() == null ? null : user.getProfile().getId();

        return new UserDto(
                user.getId(),
                user.getCreateAt(),
                user.getUpdateAt(),
                user.getName(),
                user.getEmail(),
                profileId,
                online
        );
    }
}