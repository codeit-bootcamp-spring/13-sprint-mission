package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import org.springframework.security.core.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;
  private final SessionRegistry sessionRegistry;

  public UserResponse toDto(User user) {
    if (user == null) {
      return null;
    }

    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        binaryContentMapper.toDto(user.getProfile()),
        isOnline(user.getId()),
        user.getRole()
    );
  }

  private boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(DiscodeitUserDetails.class::isInstance)
        .map(DiscodeitUserDetails.class::cast)
        .filter(principal -> principal.getUserDto().id().equals(userId))
        .anyMatch(principal -> !sessionRegistry.getAllSessions(principal, false).isEmpty());
  }
}
