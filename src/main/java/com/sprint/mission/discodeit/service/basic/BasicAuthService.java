package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.WrongPasswordException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  @Override
  public UserDto login(LoginRequest loginRequest) {
    log.debug("로그인 처리 시작");

    User user = userRepository.findByUsername(loginRequest.username())
        .orElseThrow(() -> {
          log.warn("로그인 실패: 사용자를 찾을 수 없음");
          return new UserNotFoundException(loginRequest.username());
        });

    if (!user.getPassword().equals(loginRequest.password())) {
      log.warn("로그인 실패: 비밀번호 불일치, userId={}", user.getId());
      throw new WrongPasswordException();
    }

    log.info("로그인 성공: userId={}", user.getId());

    return userMapper.toDto(user);
  }
}
