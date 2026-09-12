package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final SessionRegistry sessionRegistry;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto updateRole(UserRoleUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        user.updateRole(request.newRole());

        expireUserSessions(user.getId());

        return userMapper.toDto(user, false);
    }

    private void expireUserSessions(UUID userId) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof DiscodeitUserDetails details && userId.equals(details.getUserDto().id())) {
                List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
                sessions.forEach(SessionInformation::expireNow);
            }
        }
    }
}
