package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.auth.LoginResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.sprint.mission.discodeit.exception.user.InvalidCredentialsException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login requested: username={}", request.username());

        User user = userRepository.findByName(request.username())
                .orElseThrow(() -> new InvalidCredentialsException(request.username()));

        if (!user.getPassword().equals(request.password())) {
            log.warn("Login failed: username={}", request.username());
            throw new InvalidCredentialsException(request.username());
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                .orElse(null);

        boolean isOnline = userStatus != null && userStatus.isOnline();
        UUID profileId = user.getProfile() == null ? null : user.getProfile().getId();

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profileId,
                isOnline
        );
    }
}
