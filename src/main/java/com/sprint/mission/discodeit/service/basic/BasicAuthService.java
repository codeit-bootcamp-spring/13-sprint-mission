package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundByUserIdException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicAuthService implements AuthService {

    //필드
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserMapper userMapper;

    //interface
    @Override
    @Transactional
    public UserDto login(LoginRequest request) {
        //유저 검색
        User userTemp = userRepository.findByUsernameAndPassword(request.username(), request.password())
                .orElseThrow(() -> new UserNotFoundException(request.username(), request.password()));

        //유저 상태 검색 및 마지막 접속 시간 업데이트
        UserStatus userStatus = userStatusRepository.findByUserId(userTemp.getId())
                .orElseThrow(() -> new UserStatusNotFoundByUserIdException(userTemp.getId()));

        userStatus.updateLastActiveAt();

        log.info("유저: {} 로그인 승인.", request.username());

        return userMapper.toDto(userTemp);
    }

}
