package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;

    @Override
    public UserResponse login(LoginRequest dto) {

        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자가 없습니다."));

        if (!user.getPassword().equals(dto.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true);

    }

}
