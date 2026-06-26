package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> createUser(
            @RequestPart("userCreateRequest") UserCreateRequest request,
            @RequestPart(value = "profile", required = false) MultipartFile profile
    ) throws IOException {
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
    public ResponseEntity<UserResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAllDto().stream().toList());
    }

    @RequestMapping (value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(userId, request));
    }

    @RequestMapping (value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusResponse> updateStatus(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(userStatusService.updateByUserId(userId, request));
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public ResponseEntity<UserStatusResponse> findStatusByUserId(
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(userStatusService.findByUserId(userId));
    }
}
