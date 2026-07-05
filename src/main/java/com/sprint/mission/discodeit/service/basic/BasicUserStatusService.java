package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DuplicateResourceException;
import com.sprint.mission.discodeit.exception.ObjectNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserStatusService implements UserStatusService {

    //필드
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;

    //interface
    @Override
    @Transactional
    public UserStatusDto createUserStatus(UserStatusCreateRequest request) {
        //유저 검색
        User userTemp = userRepository.findById(request.userId())
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 유저는 데이터파일에 존재하지 않습니다."));

        //UserStatus 존재 검증
        validateUserStatusExists(request.userId());

        //UserStatus 생성
        UserStatus userStatus = new UserStatus(userTemp);
        userStatus = userStatusRepository.save(userStatus);
        log.info("UserStatus가 생성됨.");

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto findUserStatusById(UUID userStatusId) {
        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        return userStatusMapper.toDto(userStatusTemp);
    }

    @Override
    @Transactional
    public List<UserStatusDto> findAllUserStatus() {
        return userStatusRepository.findAll().stream()
                .map(userStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserStatusDto updateUserStatusByUserId(UUID userId, UserStatusUpdateRequest request) {
        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        //UserStatus 업데이트
        userStatusTemp.updateLastActiveAt();
        //dirty checking
        //userStatusTemp = userStatusRepository.save(userStatusTemp);

        return userStatusMapper.toDto(userStatusTemp);
    }

    @Override
    @Transactional
    public void deleteUserStatus(UUID userStatusId) {
        //UserStatus 검색
        UserStatus userStatusTemp = userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new ObjectNotFoundException("에러: 해당 UserStatus는 데이터파일에 존재하지 않습니다."));

        userStatusRepository.deleteById(userStatusId);

        log.info("UserStatus: {}가 삭제됨.", userStatusTemp.getId());
    }


    // 들어온 userId 필드가 레포지터리에 존재하는지 검증하는 메서드
    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("유저: " + userId + "이 존재하지 않습니다.");
        }
    }
    // 생성하려는 UserStatus가 레포지터리에 이미 존재하는지 검증하는 메서드
    private void validateUserStatusExists(UUID userId) {
        if (userStatusRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("만들려는 UserStatus가 이미 존재합니다.");
        }
    }
}
