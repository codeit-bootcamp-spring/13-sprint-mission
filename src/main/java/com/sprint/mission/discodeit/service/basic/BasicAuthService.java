package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService userDetailsService;
    private final SseService sseService;

    @Override
    public JwtRefreshResult refresh(String refreshToken) {
        if (refreshToken == null
                || !jwtTokenProvider.validateRefreshToken(
                refreshToken
        )) {
            throw new InvalidRefreshTokenException();
        }

        String username =
                jwtTokenProvider.getUsername(refreshToken);

        DiscodeitUserDetails userDetails;

        try {
            userDetails = (DiscodeitUserDetails)
                    userDetailsService.loadUserByUsername(
                            username
                    );
        } catch (UsernameNotFoundException exception) {
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken =
                jwtTokenProvider.generateAccessToken(
                        userDetails
                );

        String newRefreshToken =
                jwtTokenProvider.generateRefreshToken(
                        userDetails
                );

        JwtDto jwtDto = new JwtDto(
                userDetails.getUser(),
                newAccessToken
        );

        return new JwtRefreshResult(
                jwtDto,
                newRefreshToken
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(
            cacheNames = "users",
            allEntries = true
    )
    public UserResponse updateRole(
            UserRoleUpdateRequest request
    ) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(
                        () -> new UserNotFoundException(
                                request.userId()
                        )
                );

        UserRole previousRole = user.getRole();

        user.updateRole(request.role());

        if (previousRole != user.getRole()) {
            eventPublisher.publishEvent(
                    new RoleUpdatedEvent(
                            user.getId(),
                            previousRole,
                            user.getRole()
                    )
            );
        }

        UserResponse response =
                userMapper.toDto(user);

        sseService.broadcast(
                "users.updated",
                response
        );

        log.info(
                "사용자 권한 변경 완료: userId={}, role={}",
                user.getId(),
                user.getRole()
        );

        return response;
    }
}