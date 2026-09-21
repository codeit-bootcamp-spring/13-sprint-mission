package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateRole(
            UserRoleUpdateRequest request
    ) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(
                        () -> new UserNotFoundException(
                                request.userId()
                        )
                );

        user.updateRole(request.role());

        log.info(
                "사용자 권한 변경 완료: userId={}, role={}",
                user.getId(),
                user.getRole()
        );

        return userMapper.toDto(user);
    }
}