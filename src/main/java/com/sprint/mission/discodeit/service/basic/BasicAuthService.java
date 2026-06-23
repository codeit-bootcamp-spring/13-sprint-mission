package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.exception.WrongPasswordException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@Primary
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;

  @Override
  public UserResponse login(LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.username())
        .orElseThrow(() -> new UserNotFoundException(loginRequest.username()));

    if (!user.getPassword().equals(loginRequest.password())) {
      throw new WrongPasswordException();
    }
    return convertToResponse(user);
  }

  private UserResponse convertToResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getUsername(),
        user.getEmail(),
        user.getPassword(),
        user.getProfileId()
    );
  }
}
