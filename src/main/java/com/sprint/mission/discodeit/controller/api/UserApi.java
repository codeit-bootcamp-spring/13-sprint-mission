package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "User API")
@RequestMapping("/api/users")
public interface UserApi {

  @Operation(summary = " 사용자 생성")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<User> create(
      @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false)
      MultipartFile profile
  );

  @Operation(summary = "사용자 목록 조회")
  @GetMapping
  ResponseEntity<List<UserDto>> findALl();

  @Operation(summary = "사용자 수정")
  @PatchMapping(
      value = "/{userId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  ResponseEntity<User> update(
      @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") userUpdateRequest request,
      @RequestPart(value = "profile", required = false)
      MultipartFile profile
  );

  @Operation(summary = "사용자 삭제")
  @DeleteMapping("/{userId}")
  ResponseEntity<Void> delete(
      @PathVariable UUID userId
  );

  @Operation(summary = "사용자 상태 변경")
  @PatchMapping("/{userId}/userStatus")
  ResponseEntity<UserStatus> updateUserStatusByUserId(
      @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request
  );
}