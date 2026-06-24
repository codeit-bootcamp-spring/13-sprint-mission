package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserStatusController {

    private final UserStatusService userStatusService;

    @RequestMapping(value = "/{userId}/status", method = RequestMethod.PUT)
    public UserStatusResponse updateStatus(@PathVariable UUID userId) {
        return userStatusService.updateByUserId(userId);
    }

}
