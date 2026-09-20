package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SessionRegistry sessionRegistry;

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserDto updateRole(RoleUpdateRequest request) {
    UUID userId = request.userId();
    log.debug("사용자 권한 수정 시작: userId={}, newRole={}", userId, request.newRole());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    user.updateRole(request.newRole());
    expireSessions(userId);

    log.info("사용자 권한 수정 완료: userId={}, role={}", userId, user.getRole());
    return userMapper.toDto(user);
  }

  /**
   * 권한이 변경된 사용자가 로그인 상태라면 세션을 만료시킨다.
   * 이미 발급된 세션에는 변경 전 권한이 담겨 있어 다시 로그인해야 하기 때문이다.
   */
  private void expireSessions(UUID userId) {
    sessionRegistry.getAllPrincipals().stream()
        .filter(DiscodeitUserDetails.class::isInstance)
        .map(DiscodeitUserDetails.class::cast)
        .filter(principal -> principal.getUserDto().id().equals(userId))
        .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
        .forEach(SessionInformation::expireNow);
  }
}
