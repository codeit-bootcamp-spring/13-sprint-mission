package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  @Override
  public UserDto login(LoginRequest loginRequest) {
    String username = loginRequest.username();
    String password = loginRequest.password();

    log.debug("로그인 요청 - username={}", username);

    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
              log.warn("로그인 실패 - 존재하지 않는 username={}", username);
              return new com.sprint.mission.discodeit.exception.DiscodeitException(
                      ErrorCode.USER_NOT_FOUND,
                      java.util.Map.of("username", username)
              );
            });

    if (!user.getPassword().equals(password)) {
      log.warn("로그인 실패 - 잘못된 비밀번호 username={}", username);
      // AUTH_INVALID_PASSWORD는 ErrorCode에 이미 정의해뒀어요
      throw new com.sprint.mission.discodeit.exception.DiscodeitException(
              ErrorCode.AUTH_INVALID_PASSWORD,
              java.util.Map.of("username", username)
      );
    }

    log.info("로그인 성공 - username={}", username);
    return userMapper.toDto(user);
  }
}