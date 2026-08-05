package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @RequestPart("userCreateRequest") @Valid UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    log.debug(
        "사용자 생성 요청: hasProfile={}",
        profile != null && !profile.isEmpty()
    );

    UserDto response = userService.create(request, profile);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @PathVariable("userId") UUID id,
      @RequestPart("userUpdateRequest") @Valid UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    log.debug(
        "사용자 수정 요청: userId={}, hasProfile={}",
        id,
        profile != null && !profile.isEmpty()
    );

    UserDto response = userService.update(id, request, profile);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(
      @PathVariable("userId") UUID id
  ) {
    log.debug("사용자 단건 조회 요청: userId={}", id);

    UserDto response = userService.findById(id);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    log.debug("사용자 목록 조회 요청");

    List<UserDto> response = userService.findAll();

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(
      @PathVariable("userId") UUID id
  ) {
    log.debug("사용자 삭제 요청: userId={}", id);

    userService.delete(id);

    return ResponseEntity.noContent().build();
  }
}



