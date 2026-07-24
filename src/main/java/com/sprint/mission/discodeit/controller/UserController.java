package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.util.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.*;
import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

   @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> create(
           @Valid @RequestPart CreateUserRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
       CreateBinaryContentCommand profileImageCommand =
               FileUtils.toCommand(profileImage)
                       .orElse(null);

       UserDto userResponse = userService.create(
               request.toCommand(),
               profileImageCommand
       );

       return ResponseEntity
               .status(HttpStatus.CREATED)
               .body(userResponse);
   }

    @PatchMapping(value = "/{userId}",
                    consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> update(@PathVariable UUID userId,
                          @Valid @RequestPart UpdateUserRequest request,
                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        CreateBinaryContentCommand profileImageCommand =
                FileUtils.toCommand(profileImage)
                        .orElse(null);

        UserDto response = userService.update(
                userId,
                request.toCommand(),
                profileImageCommand
        );

        return ResponseEntity.ok(response);

    }

    @DeleteMapping(value = "/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> findAll() {
       List<UserDto> dto = userService.findAll();
        return ResponseEntity.ok(dto);
    }

    @PatchMapping(value = "/{userId}/status")
    public ResponseEntity<UserStatusDto> updateStatus(
            @PathVariable UUID userId,
            @Valid @RequestPart UpdateUserStatusCommand command
    ) {
        UserStatusDto statusDto = userStatusService.updateByUserId(userId, command);
        return ResponseEntity.ok(statusDto);
    }
}


