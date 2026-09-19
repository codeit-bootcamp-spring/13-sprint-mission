package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.jwt.InvalidJwtException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;

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

    @Override
    public JwtDto refresh(String refreshToken) {
        // refresh 토큰 검증
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new InvalidJwtException();
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        // Todo jwtRegistry에서도 refresh 토큰 검증

        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) discodeitUserDetailsService.loadUserByUsername(username);
        Role role = userDetails.getUserDto().role();

        String newAccessToken = jwtTokenProvider.createAccessToken(username, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username, role);

        // Todo jwtRegistry에서 리프레시 토큰 로테이션 진행

        return new JwtDto(userDetails.getUserDto(), newAccessToken);
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
