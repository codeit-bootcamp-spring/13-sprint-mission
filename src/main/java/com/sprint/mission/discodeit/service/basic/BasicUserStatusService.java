package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserStatusService implements UserStatusService {

    //필드
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    //interface
    @Override
    public UserStatus createUserStatus(UserStatusCreateRequest request) {
        //입력값 검증 처리하겠습니다
        validateUUID(request.userId());

        //존재하는 유저인지 검증
        validateUserExists(request.userId());

        //UserStatus 존재 검증
        validateUserStatusExists(request.userId());

        //UserStatus 생성
        UserStatus userStatus = new UserStatus(request.userId());
        userStatusRepository.createUserStatus(userStatus);
        log.info("UserStatus가 생성됨.");

        return userStatus;
    }

    @Override
    public UserStatus findUserStatusById(UUID userStatusId) {
        //입력값 검증 처리하겠습니다
        validateUUID(userStatusId);

        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findUserStatusById(userStatusId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        return userStatusTemp;
    }

    @Override
    public List<UserStatus> findAllUserStatus() {
        //UserStatus들 검색
        List<UserStatus> userStatusList = userStatusRepository.findAllUserStatus();

        return userStatusList;
    }

    @Override
    public UserStatusUpdateResponse updateUserStatus(UUID userStatusId, UserStatusUpdateRequest request) {
        //입력값 검증 처리하겠습니다
        validateUUID(userStatusId);
        validateUUID(request.userId());

        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findUserStatusById(userStatusId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        //UserStatus 업데이트
        userStatusTemp.updateLastAccessTime();
        userStatusRepository.save();

        return UserStatusUpdateResponse.from(userStatusTemp);
    }

    @Override
    public UserStatusUpdateResponse updateUserStatusByUserId(UUID userId, UserStatusUpdateRequest request) {
        //입력값 검증 처리하겠습니다
        validateUUID(userId);

        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findUserStatusByUserId(userId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        //UserStatus 업데이트
        userStatusTemp.updateLastAccessTime();
        userStatusRepository.save();

        return UserStatusUpdateResponse.from(userStatusTemp);
    }

    @Override
    public void deleteUserStatus(UUID userStatusId) {
        //입력값 검증 처리하겠습니다
        validateUUID(userStatusId);

        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findUserStatusById(userStatusId)
                .orElseThrow(() -> new RuntimeException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        userStatusRepository.deleteUserStatus(userStatusId);

        log.info("UserStatus: {}가 삭제됨.", userStatusTemp.getId());
    }


    // 들어온 UUID 필드가 null인지 검증하는 메서드
    private void validateUUID(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("에러: 입력값이 Null입니다.");
        }
    }
    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new RuntimeException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 생성하려는 UserStatus가 레포지터리에 이미 존재하는지 검증하는 메서드
    private void validateUserStatusExists(UUID userId) {
        if (userStatusRepository.existsUserStatusByUserId(userId)) {
            throw new RuntimeException("만들려는 UserStatus가 이미 존재합니다.");
        }
    }
}
