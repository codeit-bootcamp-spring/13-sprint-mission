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
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.userName())
                                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if(request.password().equals(user.getPassword())) {
            return new UserResponse(user.getId(), request.userName(), user.getEmail(), true);
        }
        throw new IllegalArgumentException("비밀번호가 맞지 않습니다.");
    }
}
