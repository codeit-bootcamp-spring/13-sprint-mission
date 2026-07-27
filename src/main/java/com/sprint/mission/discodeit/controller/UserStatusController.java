package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserStatusController {

  private final UserStatusService userStatusService;

  @PatchMapping("/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateStatus(
      @PathVariable("userId") UUID userId,
      @RequestBody @Valid UserStatusUpdateRequest request
  ) {
    UserStatusDto response = userStatusService.updateByUserId(userId, request);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> findByUserId(@PathVariable("userId") UUID userId) {
    UserStatusDto response = userStatusService.findByUserId(userId);
    return ResponseEntity.ok(response);
  }
}
