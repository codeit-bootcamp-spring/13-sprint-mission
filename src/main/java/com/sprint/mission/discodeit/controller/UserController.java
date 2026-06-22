package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserListResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> create(
      @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {

    UserResponse response = userService.create(request, profile);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserResponse> update(
      @PathVariable("userId") UUID id,
      @RequestPart UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    UserResponse response = userService.update(id, request, profile);

    return ResponseEntity.ok().body(response);
  }

  @GetMapping
  public ResponseEntity<List<UserListResponse>> findAll() {
    List<UserListResponse> all = userService.findAll();

    return ResponseEntity.ok(all);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(@PathVariable("userId") UUID id) {
    userService.delete(id);

    return ResponseEntity.noContent().build();
  }

}



