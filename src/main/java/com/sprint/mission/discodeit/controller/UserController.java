package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController // RESTful API로 다시 설계
@RequiredArgsConstructor
@RequestMapping("/api/users") // 컨트롤러에 공통 url 매핑, 기본 시작을 지정
@Tag(name = "User", description = "User API") // 그룹 묶기
public class UserController {

  // Controller -> Service -> Repository
  private final UserService userService;
  private final UserStatusService userStatusService;
  // 의존성 관계 추가

  // 사용자 등록
  @Operation(summary = "User 등록") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함")})
  @RequestMapping(
      method = RequestMethod.POST,
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ResponseEntity<UserDto> create(
      @Valid @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,
      @Parameter(schema = @Schema(type = "string", format = "binary"), description = "User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .map(file -> {
          try {
            return new BinaryContentCreateRequest(file.getOriginalFilename(),
                file.getContentType(), file.getBytes());

          } catch (IOException e) {
            throw new IllegalArgumentException(e);
          }
        });
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userService.create(userCreateRequest, profileRequest));
  }

  // 사용자 정보 수정
  @Operation(summary = "User 정보 수정") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음"),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함")})
  @RequestMapping(
      value = "/{userId}", method = RequestMethod.PATCH,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @Parameter(name = "userId", in = ParameterIn.PATH, description = "수정할 User ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID userId,
      @Valid @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest,
      @Schema(type = "string", format = "binary", description = "수정할 User 프로필 이미지")
      @RequestPart(value = "profile", required = false) MultipartFile profile) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(profile)
        .map(file -> {
          try {
            return new BinaryContentCreateRequest(file.getOriginalFilename(),
                file.getContentType(), file.getBytes());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.update(userId, userUpdateRequest, profileRequest));
  }

  // 사용자 삭제
  @Operation(summary = "User 삭제") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음")})
  @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(
      @Parameter(name = "userId", in = ParameterIn.PATH, description = "삭제할 User ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  // 모든 사용자 조회
  @Operation(summary = "전체 User 목록 조회") // 엔드포인트 설명
  @ApiResponse(
      responseCode = "200",
      description = "User 목록 조회 성공",
      content = @Content(
          array = @ArraySchema(
              schema = @Schema(implementation = UserDto.class))))
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<UserDto>> findAll() {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userService.findAll());
  }

  // 사용자의 온라인 상태 업데이트
  @Operation(summary = "User 온라인 상태 업데이트") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User 온라인 상태가 성공적으로 업데이트됨"),
      @ApiResponse(responseCode = "404", description = "해당 User의 UserStatus를 찾을 수 없음")})
  @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
  public ResponseEntity<UserStatusDto> updateOnlineStatus(
      @Parameter(name = "userId", in = ParameterIn.PATH, description = "상태를 변경할 User ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID userId,
      @Valid @RequestBody UserStatusUpdateRequest request) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userStatusService.updateByUserId(userId, request));
  }
}
