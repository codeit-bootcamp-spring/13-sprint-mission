package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @RequestPart("userCreateRequest") UserRequest userRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {

        UserResponse response = userService.create(userRequest, toBinaryContentRequest(profile));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> responses = userService.findAll();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("userId") UUID userId,
            @RequestPart("userUpdateRequest") UserRequest userRequest,
            @RequestPart(value = "profile", required = false) MultipartFile profile) {

        UserResponse response = userService.update(userId, userRequest, toBinaryContentRequest(profile));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/userStatus")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("userId") UUID userId,
            @RequestBody UserRequest request) {

        UserResponse response = userService.update(userId, request, null);
        return ResponseEntity.ok(response);
    }

    private BinaryContentRequest toBinaryContentRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            return new BinaryContentRequest(
                    fileName == null || fileName.isBlank() ? "profile" : fileName,
                    file.getSize(),
                    contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType,
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new UncheckedIOException("프로필 파일을 읽을 수 없습니다.", e);
        }
    }

}