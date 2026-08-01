package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.InvalidCredentialsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto login(String username, String password) { //validation으로 유효성 검증 완.
    log.info("로그인 요청 - username: {}", username);

    User user = userRepository.findByUserName(username)
        .orElseThrow(() -> new InvalidCredentialsException(username));

    if (!user.getPassword().equals(password)) {
      throw new InvalidCredentialsException(username);
    }

    user.getUserStatus().updateLastActiveAt(Instant.now());

    log.info("로그인 성공 - username: {}", username);
    return userMapper.toDto(user);
  }
}