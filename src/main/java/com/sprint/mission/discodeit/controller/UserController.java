package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> create(@RequestPart("userCreateRequest") UserCreateRequest request,
                                          @RequestPart(value = "profile", required = false) MultipartFile profile)
            throws IOException {
        UserCreateRequest createRequest = new UserCreateRequest(
                request.username(),
                request.email(),
                request.password(),
                toBinaryContentCreateRequest(profile)
        );

        return ResponseEntity.status(201).body(userService.create(createRequest));
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> update(@PathVariable UUID userId,
                                               @RequestPart("userUpdateRequest") UserUpdateRequest request,
                                               @RequestPart(value = "profile", required = false) MultipartFile profile)
            throws IOException {
        UserUpdateRequest updateRequest = new UserUpdateRequest(
                userId,
                request.newUsername(),
                request.newEmail(),
                request.newPassword(),
                toBinaryContentCreateRequest(profile)
        );

        return ResponseEntity.ok(userService.update(updateRequest));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PatchMapping("/{userId}/userStatus")
    public ResponseEntity<UserStatusDto> updateOnlineStatus(@PathVariable UUID userId,
                                                            @RequestBody UserStatusUpdateByUserIdRequest request) {
        UserStatusUpdateByUserIdRequest updateRequest =
                new UserStatusUpdateByUserIdRequest(userId, request.newLastActiveAt());

        return ResponseEntity.ok(userStatusService.updateByUserId(updateRequest));
    }

    private BinaryContentCreateRequest toBinaryContentCreateRequest(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.getContentType();

        return new BinaryContentCreateRequest(
                file.getOriginalFilename(),
                file.getSize(),
                contentType,
                file.getBytes()
        );
    }
}
