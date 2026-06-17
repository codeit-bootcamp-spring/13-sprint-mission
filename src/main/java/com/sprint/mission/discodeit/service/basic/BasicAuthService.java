package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.AuthLoginRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@RequiredArgsConstructor // 의존성 주입
@Service
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository statusRepository;

    @Override
    public UserResponse login(AuthLoginRequest request) {
        // username, password 일치하는 유저 -> 유저 정보 반환
        // 유저 정보 얻기 위해 filter
        User user=userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(request.username())
                && u.getPassword().equals(request.password()))
                        .findFirst()
                                .orElseThrow(()->new NoSuchElementException("존재하지 않는 아이디, 비밀번호입니다.")); // 일치하는 유저 없는 경우 -> 예외 발생
        UserStatus userStatus = statusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자입니다."));
        return UserResponse.from(user, userStatus);
    }
}
