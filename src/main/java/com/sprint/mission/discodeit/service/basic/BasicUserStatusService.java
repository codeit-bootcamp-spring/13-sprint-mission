package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.*;
@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository repository;

    @Override
    public UserStatusResponse create(CreateUserStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("유저 상태 생성 요청은 필수입니다.");
        }

        User user = repository.find(request.userId());

        if (user == null) {
            throw new IllegalArgumentException("유저가 없습니다.");
        }

        UserStatus existingUserStatus =
                userStatusRepository.findByUserId(request.userId());

        if (existingUserStatus != null) {
            throw new IllegalArgumentException("이미 해당 유저의 상태 정보가 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(request.userId());

        userStatusRepository.create(userStatus);

        return UserStatusResponse.from(userStatus);
    }
    @Override
    public UserStatusResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저아이디가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.find(id);

        if (userStatus == null) {
            throw new IllegalArgumentException("해당 유저의 상태 정보가 없습니다.");
        }

        return UserStatusResponse.from(userStatus);
    }

    @Override
    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll().stream()
                .map(UserStatusResponse::from)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("삭제할 아이디가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.find(id);

        if (userStatus == null) {
            throw new IllegalArgumentException("삭제할 유저의 상태 정보가 없습니다.");
        }

        userStatusRepository.delete(id);
    }

    @Override
    public UserStatusResponse update(UUID id, UpdateUserStatusRequest request) {
        if(id == null) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("업데이트할 유저가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.find(id);

        if (userStatus == null) {
            throw new IllegalArgumentException("업데이트 유저의 정보가 없습니다.");
        }

        userStatus.updateLastOnlineAt(request.lastOnlineTime());

        userStatusRepository.update(userStatus);

        return UserStatusResponse.from(userStatus);


    }

    @Override
    public void updateByUserId(UUID userId, UpdateUserStatusRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 아이디는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("업데이트할 유저가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(userId);

        if (userStatus == null) {
            throw new IllegalArgumentException("업데이트할 유저 정보가 없습니다.");
        }

        userStatus.updateLastOnlineAt(request.lastOnlineTime());

        userStatusRepository.update(userStatus);
    }
}
