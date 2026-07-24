package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAllUsers() {
        List<UserResponse> userResponses = userService.findAll();
        List<UserDto> dtos = new ArrayList<>();

        for (UserResponse userResponse : userResponses) {
            User user = userService.findById(userResponse.id());

            UserDto dto = new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), user.getUserName(), user.getEmail(), user.getProfileId(), userResponse.isOnline());
            dtos.add(dto);
        }

        return ResponseEntity.ok(dtos);
    }

    @PatchMapping(value = "/{userId}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
        UserUpdateRequest dto = new UserUpdateRequest(userId, request.userName(), request.email(), request.password(), request.profile());
        return ResponseEntity.ok(userService.update(dto));
    }

    @PatchMapping(value = "/{userId}/userStatus")
    public ResponseEntity<UserStatusResponse> updateStatus(@PathVariable UUID userId) {
        UserStatusUpdateByUserIdRequest request = new UserStatusUpdateByUserIdRequest(userId, Instant.now());
        return ResponseEntity.ok(userStatusService.updateByUserId(request));
    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

}
