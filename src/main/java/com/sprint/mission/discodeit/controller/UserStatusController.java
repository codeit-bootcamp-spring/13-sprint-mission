package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserStatusController {

    private final UserStatusService userStatusService;

    /*
     * 기존 방식: userId로 사용자 상태 조회
     * GET /api/user-statuses/{userId}
     */
    @GetMapping("/api/user-statuses/{userId}")
    public ResponseEntity<UserStatusResponse> findByUserId(
            @PathVariable UUID userId
    ) {
        UserStatusResponse response =
                userStatusService.findByUserId(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 / 프론트 호환 경로
     * GET /api/users/{userId}/userStatus
     */
    @GetMapping("/api/users/{userId}/userStatus")
    public ResponseEntity<UserStatusResponse> findByUserIdForUsersPath(
            @PathVariable UUID userId
    ) {
        UserStatusResponse response =
                userStatusService.findByUserId(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * 기존 프론트 호환 경로
     * GET /api/user/{userId}/userStatus
     */
    @GetMapping("/api/user/{userId}/userStatus")
    public ResponseEntity<UserStatusResponse> findByUserIdForUserPath(
            @PathVariable UUID userId
    ) {
        UserStatusResponse response =
                userStatusService.findByUserId(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * 기존 방식: 사용자를 온라인 상태로 변경
     * PATCH /api/user-statuses/{userId}/online
     */
    @PatchMapping("/api/user-statuses/{userId}/online")
    public ResponseEntity<UserStatusResponse> updateOnline(
            @PathVariable UUID userId
    ) {
        UserStatusResponse response =
                userStatusService.updateOnline(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * 기존 방식: 사용자를 오프라인 상태로 변경
     * PATCH /api/user-statuses/{userId}/offline
     */
    @PatchMapping("/api/user-statuses/{userId}/offline")
    public ResponseEntity<UserStatusResponse> updateOffline(
            @PathVariable UUID userId
    ) {
        UserStatusResponse response =
                userStatusService.updateOffline(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     *
     * PATCH /api/users/{userId}/userStatus
     *
     * body:
     * {
     *   "newLastActiveAt": "2026-07-10T01:30:55.469015Z"
     * }
     */
    @PatchMapping("/api/users/{userId}/userStatus")
    public ResponseEntity<UserStatusResponse> updateUserStatusForUsersPath(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        /*
         * 현재 UserStatusService에 lastActiveAt을 직접 수정하는
         * 메서드가 없으므로 기존 동작인 온라인 변경을 유지한다.
         *
         * request는 @Valid를 통해 필수값과 시간 범위를 검증한다.
         */
        UserStatusResponse response =
                userStatusService.updateOnline(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * 기존 프론트 호환 경로
     *
     * PATCH /api/user/{userId}/userStatus
     */
    @PatchMapping("/api/user/{userId}/userStatus")
    public ResponseEntity<UserStatusResponse> updateUserStatusForUserPath(
            @PathVariable UUID userId,
            @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        UserStatusResponse response =
                userStatusService.updateOnline(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * 기존 방식: userId로 사용자 상태 삭제
     * DELETE /api/user-statuses/{userId}
     */
    @DeleteMapping("/api/user-statuses/{userId}")
    public ResponseEntity<Void> deleteByUserId(
            @PathVariable UUID userId
    ) {
        userStatusService.deleteByUserId(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}