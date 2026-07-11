package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserMapper {

    private final UserStatusRepository userStatusRepository;
    private final BinaryContentMapper binaryContentMapper;


    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
        Boolean online = userStatus != null && userStatus.isOnline();
        BinaryContentDto profile = binaryContentMapper.toDto(user.getProfile());

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profile,
                online
        );
    }
}