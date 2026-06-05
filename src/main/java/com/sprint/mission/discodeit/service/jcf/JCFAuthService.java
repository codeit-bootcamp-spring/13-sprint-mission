package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;

public class JCFAuthService implements AuthService {

    private final UserRepository userRepository;

    public JCFAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User login(LoginRequest loginRequest) {
        for (User user : userRepository.findAll()) {
            if (user.getEmail().equals(loginRequest.email())
                && user.getPassword().equals(loginRequest.password())) {
                return user;
            }
        }
        throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
