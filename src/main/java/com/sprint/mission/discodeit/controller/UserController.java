package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserDto> createUser(
            @RequestPart("userCreateRequest") @Valid UserCreateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {
        log.info("Received user create request: username={}, email={}", request.username(), request.email());
        UserCreateRequest serviceRequest = new UserCreateRequest(
                request.username(),
                request.email(),
                request.password(),
                profile != null ? profile.getOriginalFilename() : null,
                profile != null ? profile.getContentType() : null,
                profile != null ? profile.getBytes() : null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(serviceRequest));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<UserDto> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll().stream().toList());
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<UserDto> update(
            @PathVariable UUID userId,
            @RequestPart("userUpdateRequest") @Valid UserUpdateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {
        log.info("Received user update request: userId={}", userId);
        UserUpdateRequest serviceRequest = new UserUpdateRequest(
                request.username(),
                request.email(),
                request.password(),
                profile != null ? profile.getOriginalFilename() : null,
                profile != null ? profile.getContentType() : null,
                profile != null ? profile.getBytes() : null
        );

        return ResponseEntity.ok(userService.update(userId, serviceRequest));
    }

    @RequestMapping (value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        log.info("Received user delete request: userId={}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusDto> updateStatus(
            @PathVariable UUID userId,
            @RequestBody @Valid UserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(userStatusService.updateByUserId(userId, request));
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<UserStatusDto> findStatusByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(userStatusService.findByUserId(userId));
    }
}
