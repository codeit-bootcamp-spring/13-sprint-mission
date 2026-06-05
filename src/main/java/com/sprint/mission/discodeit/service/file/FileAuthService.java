package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import org.springframework.stereotype.Service;

public class FileAuthService implements AuthService {

    private final UserRepository userRepository;

    public FileAuthService(UserRepository userRepository) {
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
