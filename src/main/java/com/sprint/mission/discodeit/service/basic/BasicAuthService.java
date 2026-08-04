package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.LoginRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

// 인증 기능을 실제로 구현하는 Service 클래스
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    // 사용자 정보를 조회하기 위한 Repository
    private final UserRepository userRepository;

    // 사용자의 온라인 상태를 조회·수정하기 위한 Repository
    private final UserStatusRepository userStatusRepository;

    // 로그인 기능
    @Override
    public UserResponse login(LoginRequest request) {
        log.info(
                "로그인을 시작합니다. username={}",
                request == null ? null : request.getUsername()
        );

        // 로그인 요청 객체가 없는 경우
        if (request == null) {
            log.warn("로그인 요청이 비어 있습니다.");

            throw new IllegalArgumentException(
                    "로그인 요청은 비어 있을 수 없습니다."
            );
        }

        // 사용자 이름이 비어 있는 경우
        if (request.getUsername() == null
                || request.getUsername().isBlank()) {

            log.warn("로그인 사용자 이름이 비어 있습니다.");

            throw new IllegalArgumentException(
                    "사용자 이름은 비어 있을 수 없습니다."
            );
        }

        // 비밀번호가 비어 있는 경우
        if (request.getPassword() == null
                || request.getPassword().isBlank()) {

            log.warn(
                    "로그인 비밀번호가 비어 있습니다. username={}",
                    request.getUsername()
            );

            throw new IllegalArgumentException(
                    "비밀번호는 비어 있을 수 없습니다."
            );
        }

        // username으로 사용자 조회
        User user = userRepository.findByUsername(
                request.getUsername()
        );

        /*
         * 사용자가 없거나 비밀번호가 일치하지 않는 경우
         * 계정 존재 여부가 노출되지 않도록 같은 메시지를 반환한다.
         */
        if (user == null
                || !Objects.equals(
                user.getPassword(),
                request.getPassword()
        )) {

            log.warn(
                    "로그인에 실패했습니다. username={}",
                    request.getUsername()
            );

            throw new IllegalArgumentException(
                    "사용자 이름 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 로그인 성공 시 사용자 상태 조회
        UserStatus userStatus =
                userStatusRepository.findByUserId(user.getId());

        // 상태 정보가 없으면 새로 생성
        if (userStatus == null) {
            userStatus = new UserStatus(user.getId());
        }

        // 온라인 상태 및 마지막 활동 시간 갱신
        userStatus.updateOnline();
        userStatusRepository.save(userStatus);

        log.info(
                "로그인이 완료되었습니다. userId={}, username={}",
                user.getId(),
                user.getUsername()
        );

        return toResponse(user);
    }

    // User 엔티티를 UserResponse DTO로 변환
    private UserResponse toResponse(User user) {
        UserStatus userStatus =
                userStatusRepository.findByUserId(user.getId());

        boolean online =
                userStatus != null && userStatus.isOnline();

        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                online
        );
    }
}