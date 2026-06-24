package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        //유저 검색
        User userTemp = userRepository.findUserByNameAndPassword(request.username(), request.password())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //유저 상태 검색 및 마지막 접속 시간 업데이트
        UserStatus userStatus = userStatusRepository.findUserStatusByUserId(userTemp.getId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저의 온라인 상태를 불러올 수 없습니다."));

        userStatus.updateLastActiveAt();
        userStatusRepository.save();

        log.info("유저: {} 로그인 승인.", request.username());

        return userTemp;
    }

}
