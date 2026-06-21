package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getName().equals(loginRequest.name()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));
        if (!user.getPassword().equals(loginRequest.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));


        return new UserResponse(
                user.getUserId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getName(),
                user.getEmail(),
                user.getProfileId(),
                userStatus.isOnline()
        );
    }
}
