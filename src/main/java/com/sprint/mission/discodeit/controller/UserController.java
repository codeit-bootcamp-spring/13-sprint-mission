package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.FileUploadDto;
import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  @Operation(summary = "전체 User 목록 조회")
  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.findAllUsers());
  }

  @Operation(summary = "User 등록")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(
          mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
          encoding = @Encoding(
              name = "userCreateRequest",
              contentType = MediaType.APPLICATION_JSON_VALUE
          )
      )
  )
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> createUser(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    UserDto response = userService.create(
        request.email(),
        request.username(),
        request.password(),
        convertToFileUploadDto(profile)
    );
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "User 정보 수정")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(
          mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
          encoding = @Encoding(
              name = "userUpdateRequest",
              contentType = MediaType.APPLICATION_JSON_VALUE
          )
      )
  )
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID userId,
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile) {

    UserDto response = userService.update(
        userId,
        request.newEmail(),
        request.newUsername(),
        request.newPassword(),
        request.statusMessage(),
        convertToFileUploadDto(profile)
    );
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "User 삭제")
  @ApiResponse(responseCode = "204", description = "No Content")
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "User 온라인 상태 업데이트")
  @PatchMapping("/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateUserStatus(
      @PathVariable UUID userId, @Valid @RequestBody UserStatusUpdateRequest request) {

    UserStatusDto response = userStatusService.updateByUserId(userId, request.newLastActiveAt());
    return ResponseEntity.ok(response);
  }

  private FileUploadDto convertToFileUploadDto(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    try {
      return new FileUploadDto(
          file.getOriginalFilename(),
          file.getContentType(),
          file.getSize(),
          file.getBytes()
      );
    } catch (Exception e) {
      throw new RuntimeException("프로필 이미지 읽기 실패", e);
    }
  }
}