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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping (method =  RequestMethod.POST)
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<UserResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAllDto().stream().toList());
    }

    @RequestMapping (method = RequestMethod.PUT)
    public ResponseEntity<UserResponse> update(@RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @RequestMapping (value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping (value = "/status", method = RequestMethod.PUT)
    public ResponseEntity<UserStatusResponse> updateStatus(@RequestBody UserStatusUpdateRequest request) {
        return ResponseEntity.ok(userStatusService.update(request));
    }
}
