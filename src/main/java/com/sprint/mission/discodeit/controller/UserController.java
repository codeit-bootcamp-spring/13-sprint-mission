package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 1. 사용자 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @RequestPart("userCreateRequest") UserRequest userRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {

        UserResponse response = userService.create(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 전체 사용자 조회
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> responses = userService.findAll();
        return ResponseEntity.ok(responses);
    }

    // 3. 사용자 정보 수정
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("userId") UUID userId,
            @RequestPart("userUpdateRequest") UserRequest userRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {

        UserResponse response = userService.update(userId, userRequest);
        return ResponseEntity.ok(response);
    }

    // 4. 사용자 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    // 5.
    // [ ] 사용자의 온라인 상태를 업데이트할 수 있다.
    @PatchMapping("/{userId}/userStatus")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("userId") UUID userId,
            @RequestBody UserRequest request) {

        UserResponse response = userService.update(userId, request);
        return ResponseEntity.ok(response);
    }
}

// [ ]  지금까지 구현한 서비스 로직을 활용해 웹 API를 구현하세요.
//      이때 @RequestMapping만 사용해 구현해보세요.