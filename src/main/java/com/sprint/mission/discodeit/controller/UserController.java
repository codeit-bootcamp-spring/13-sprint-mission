package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserCreateRequest request) {
        log.info("사용자 생성 요청: username={}", request.userName());
        return ResponseEntity.ok(userService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PatchMapping(value = "/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        log.info("사용자 수정 요청: userId={}", userId);
        UserUpdateRequest dto = new UserUpdateRequest(userId, request.userName(), request.email(), request.password(), request.profile());
        return ResponseEntity.ok(userService.update(dto));
    }

    @PatchMapping(value = "/{userId}/userStatus")
    public ResponseEntity<UserStatusDto> updateStatus(@PathVariable UUID userId) {
        UserStatusUpdateByUserIdRequest request = new UserStatusUpdateByUserIdRequest(userId, Instant.now());
        return ResponseEntity.ok(userStatusService.updateByUserId(request));
    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        log.info("사용자 삭제 요청: userId={}", userId);
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

}
