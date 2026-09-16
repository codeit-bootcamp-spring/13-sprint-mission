package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.security.CustomUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;
  private final SessionRegistry sessionRegistry;

  public UserDto toDto(User user) {
    if (user == null) {
      return null;
    }

    boolean online = isOnline(user.getId());

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        binaryContentMapper.toDto(user.getProfile()),
        online,
        user.getRole()
    );
  }

  private boolean isOnline(UUID userId) {
    return sessionRegistry.getAllPrincipals().stream()
        .filter(CustomUserDetails.class::isInstance)
        .map(CustomUserDetails.class::cast)
        .filter(UserDetails ->
            UserDetails.getUserDto().id().equals(userId)
        )
        .anyMatch(userDetails ->
            !sessionRegistry.getAllSessions(userDetails, false).isEmpty()
        );
  }
}
