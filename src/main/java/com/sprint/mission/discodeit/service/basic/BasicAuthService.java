package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.auth.LoginCommand;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.auth.InvalidCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto login(LoginCommand command) {
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new InvalidCredentialsException());
        if (!user.getPassword().equals(command.password())) {
            throw new InvalidCredentialsException();
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                .orElse(null);

        log.info("로그인 성공 id : {}", user.getId());
        return userMapper.toDto(user, userStatus);
    }
}
