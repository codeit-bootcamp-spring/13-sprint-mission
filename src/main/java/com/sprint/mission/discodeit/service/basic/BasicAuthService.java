package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  public UserDto login(String username, String password) {
    User user = userRepository.findAll().stream()
        .filter(u -> u.getUserName().equals(username) && u.getPassword().equals(password))
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("username 또는 password가 틀렸습니다."));

    userStatusRepository.findByUserId(user.getId())
        .ifPresent(s -> {
          s.updateLastActiveAt(Instant.now());
          userStatusRepository.save(s);
        });

    boolean online = userStatusRepository.findByUserId(user.getId())
        .map(s -> s.getLastActiveAt().isAfter(Instant.now().minusSeconds(300)))
        .orElse(false);

    return new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), user.getUserName(),
        user.getEmail(),
        user.getProfileId(), online);
  }
}
