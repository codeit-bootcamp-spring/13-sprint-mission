package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
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

  //생성
  @Operation(summary = "User 등록")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(examples = @ExampleObject(value = "User with email {email} already exists")))
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<UserDto> create(
      @Parameter(description = "User 생성 정보") @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
      @Parameter(description = "User 프로필 이미지") @RequestPart(value = "profile", required = false)
      MultipartFile profile
  );

  //조회
  @Operation(summary = "전체 User 목록 조회")
  @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
  @GetMapping
  ResponseEntity<List<UserDto>> findAll();

  //수정
  @Operation(summary = "User 정보 수정")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(examples = @ExampleObject(value = "user with email {newEmail} already exists"))),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "User with id {userId} not found")))
  })
  @PatchMapping(
      value = "/{userId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  ResponseEntity<UserDto> update(
      @Parameter(description = "수정할 User ID") @PathVariable UUID userId,
      @Parameter(description = "수정할 User 정보") @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @Parameter(description = "수정할 User 프로필 이미지") @RequestPart(value = "profile", required = false)
      MultipartFile profile
  );

  //삭제
  @Operation(summary = "User 삭제")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "User with id {id} not found")))
  })
  @DeleteMapping("/{userId}")
  ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 User ID") @PathVariable UUID userId
  );

  //상태 변경
  @Operation(summary = "User 온라인 상태 업데이트")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User온라인 상태가 성공적으로 업데이트됨"),
      @ApiResponse(responseCode = "404", description = "해당 User의 UserStatus를 찾을 수 없음",
          content = @Content(examples = @ExampleObject(value = "UserStatus with userId {userId} not found")))
  })
  @PatchMapping("/{userId}/userStatus")
  ResponseEntity<UserStatusDto> updateUserStatusByUserId(
      @Parameter(description = "상태를 변경할 User ID") @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request
  );
}