package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)
    public UserResponse create(@RequestBody UserRequest.CreateUserRequest request) {
        return userService.create(request);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT)
    public UserResponse update(@PathVariable UUID userId,
                               @RequestBody UserRequest.UpdateUserRequest request) {
        return userService.update(userId, request);
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID userId) {
        userService.delete(userId);
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @RequestMapping(
            value = "/{userId}/status",
            method = RequestMethod.PATCH
    )
    public UserStatusResponse updateStatus(
            @PathVariable UUID userId,
            @RequestBody UpdateUserStatusRequest request
    ) {
        return userStatusService.updateByUserId(userId, request);
    }
}


