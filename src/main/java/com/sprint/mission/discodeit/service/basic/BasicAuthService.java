package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.AuthUserNotFoundException;
import com.sprint.mission.discodeit.exception.auth.InvalidPasswordException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto login(LoginRequest request) {
        User user = userRepository.findByUsername(request.userName())
                                    .orElseThrow(() -> new AuthUserNotFoundException(request.userName()));

        if(request.password().equals(user.getPassword())) {
            user.getStatus().updateActiveTime(Instant.now());
            return userMapper.toDto(user);
        }
        throw new InvalidPasswordException(user.getId());
    }
}
