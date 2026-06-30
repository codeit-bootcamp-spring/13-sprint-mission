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
import org.springframework.web.multipart.*;

import javax.print.attribute.standard.*;
import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public UserResponse create(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {

        UserRequest.CreateUserRequest request =
                new UserRequest.CreateUserRequest(
                        username,
                        email,
                        password,
                        profileImage
                );

        return userService.create(request);
    }

    @RequestMapping(value = "/{userId}",
                    method = RequestMethod.PATCH,
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse update(@PathVariable UUID userId,
                               @RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        return userService.update(
                userId,
                new UserRequest.UpdateUserRequest(
                        username, email, password, profileImage));
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
            @PathVariable UUID userId
    ) {
        return userStatusService.updateByUserId(userId);
    }
}


