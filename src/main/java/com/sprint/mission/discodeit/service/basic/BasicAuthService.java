package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.request.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;

    @Override
    public UserResponse login(LoginRequest dto) {
        // [요구사항] username 과 일치하는 유저가 있는지 확인
        User user = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(dto.username()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자가 없습니다."));
        // [요구사항] password 가 일치하는지 확인
        if (user.getPassword().equals(dto.password())) {
            // [요구사항] 일치하는 유저(비밀번호 불일치 포함)가 없는 경우: 예외 발생
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        // [요구사항] 일치하는 유저가 있는 경우: 패스워드를 제외한 유저 정보 반환
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true);

    }

}
