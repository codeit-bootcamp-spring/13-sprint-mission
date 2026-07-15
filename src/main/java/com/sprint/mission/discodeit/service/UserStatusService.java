package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserStatusResponse;

import java.time.Instant;
import java.util.UUID;

// UserStatus 관련 기능을 정의하는 Service 인터페이스
// UserStatus는 사용자의 온라인 여부와 마지막 접속 시간을 관리함
public interface UserStatusService {

    // userId로 사용자의 상태 정보 조회
    // UserStatus의 id가 아니라 User의 id를 기준으로 조회함
    UserStatusResponse findByUserId(UUID userId);

    // 사용자를 온라인 상태로 변경
    // 로그인 성공 시 호출할 수 있음
    UserStatusResponse updateOnline(UUID userId);

    // 사용자를 오프라인 상태로 변경
    // 로그아웃 또는 접속 종료 시 호출할 수 있음
    UserStatusResponse updateOffline(UUID userId);

    /*
     * API 명세 v1.2 기준
     * PATCH /api/users/{userId}/userStatus
     *
     * 요청 body의 newLastActiveAt 값을 실제 UserStatus에 반영하기 위한 메서드
     */
    UserStatusResponse updateLastActiveAt(
            UUID userId,
            Instant lastActiveAt
    );

    // userId로 사용자의 상태 정보 삭제
    // 사용자가 삭제될 때 UserStatus도 함께 삭제할 때 사용
    void deleteByUserId(UUID userId);
}