package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(cacheNames = "users", allEntries = true)
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

        log.info(
                "사용자 권한 변경 완료: userId={}, role={}",
                user.getId(),
                user.getRole()
        );

        return userMapper.toDto(user);
    }
}