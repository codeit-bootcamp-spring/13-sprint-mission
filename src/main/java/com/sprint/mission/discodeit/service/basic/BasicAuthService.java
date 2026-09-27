package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtRegistry jwtRegistry;

    @Override
    @Transactional(readOnly = true)
    public boolean isOnline(UUID userId) {
        if (userId == null) {
            return false;
        }

        return jwtRegistry.hasActiveJwtInformationByUserId(userId);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDto updateRole(UUID userId, Role newRole) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "사용자 ID는 필수입니다."
            );
        }

        if (newRole == null) {
            throw new IllegalArgumentException(
                    "변경할 권한은 필수입니다."
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(userId)
                );

        user.updateRole(newRole);

        jwtRegistry.invalidateJwtInformationByUserId(userId);

        log.info(
                "사용자 권한 변경 및 기존 JWT 무효화 완료. userId={}, newRole={}",
                userId,
                newRole
        );

        return userMapper.toDto(user, false);
    }
}