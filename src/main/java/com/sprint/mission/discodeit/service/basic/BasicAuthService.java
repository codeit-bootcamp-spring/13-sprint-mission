package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.auth.UserNameNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor // 의존성 주입
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto login(LoginRequest request) {
    // username, password 일치하는 유저 -> 유저 정보 반환
    // 유저 정보 얻기 위해 filter
    User user = userRepository.findAll().stream()
        .filter(u -> u.getUsername().equals(request.getUsername()))
        .findFirst()
        .orElseThrow(
            () -> new UserNameNotFoundException(request.getUsername())); // 제공되는 API 스펙에 맞추어 변경

    if (!user.getPassword().equals(request.getPassword())) {
      throw new InvalidPasswordException();
    }
    return userMapper.toDto(user);
  }
}
