package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserStatusController {
    private final UserStatusService userStatusService;

    @GetMapping("/{userId}/userStatus")
    public ResponseEntity<UserStatusDto> getByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                userStatusService.findByUserId(userId)
        );
    }

    @PatchMapping("/{userId}/userStatus")
    public ResponseEntity<UserStatusDto> updateByUserId(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        UserStatusDto userStatus =
                userStatusService.updateByUserId(
                        userId,
                        request.toCommand()
                );

        return ResponseEntity.ok(userStatus);
    }
}
