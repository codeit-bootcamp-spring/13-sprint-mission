package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {

    //필드
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    //interface
    @Override
    public User login(LoginRequest request) {
        //입력값 검증 처리하겠습니다
        validateString(request.name());
        validateString(request.password());

        //유저 검색
        User userTemp = userRepository.findUserByNameAndPassword(request.name(), request.password())
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //유저 상태 검색 및 마지막 접속 시간 업데이트
        UserStatus userStatus = userStatusRepository.findUserStatusByUserId(userTemp.getId())
                .orElseThrow(() -> new RuntimeException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다."));
        userStatus.updateLastAccessTime();

        log.info("유저: {} 로그인 승인.", request.name());

        return userTemp;
    }

    // 들어온 String 필드가 null 혹은 공백인지 검증하는 메서드
    private void validateString(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("에러: 입력값이 Null 또는 공백입니다.");
        }
    }
}
