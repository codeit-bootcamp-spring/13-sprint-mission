package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  // 사용자 등록
  @RequestMapping(method = RequestMethod.POST)
  public Object createUser(
      @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) throws IOException {

    Optional<BinaryContentCreateRequest> profileRequest = Optional.empty();

    if (profile != null && !profile.isEmpty()) {
      profileRequest = Optional.of(
          new BinaryContentCreateRequest(
              profile.getOriginalFilename(),
              profile.getContentType(),
              profile.getBytes()
          )
      );
    }

    return userService.create(
        request,
        profileRequest
    );
  }

  // 모든 사용자 조회
  @RequestMapping(method = RequestMethod.GET)
  public Object getAllUsers() {

    return userService.findAll();
  }

  // 사용자 정보 수정 (현재 createUser와 예외 throws부분이 같음 -> 나중에 시간이 되면 exception에 메서드 추가하여 사용해보기)
  @RequestMapping(
      value = "/{userId}",
      method = RequestMethod.PATCH
  )
  public Object updateUser(
      @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) throws IOException {

    Optional<BinaryContentCreateRequest> profileRequest = Optional.empty();

    if (profile != null && !profile.isEmpty()) {
      profileRequest = Optional.of(
          new BinaryContentCreateRequest(
              profile.getOriginalFilename(),
              profile.getContentType(),
              profile.getBytes()
          )
      );
    }

    return userService.update(
        userId,
        request,
        profileRequest
    );
  }

  // 사용자 삭제
  @RequestMapping(
      value = "/{userId}",
      method = RequestMethod.DELETE
  )
  public void deleteUser(
      @PathVariable UUID userId
  ) {

    userService.delete(userId);
  }

  // 온라인 상태 업데이트
  @RequestMapping(
      value = "/{userId}/userStatus",
      method = RequestMethod.PATCH
  )
  public Object updateOnlineStatus(
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request
  ) {

    return userStatusService.updateByUserId(
        userId,
        request
    );
  }
}