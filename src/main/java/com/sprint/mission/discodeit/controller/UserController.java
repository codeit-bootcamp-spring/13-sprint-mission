package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.UserApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@RestController
@RequestMapping("/api/usres")//이 컨트롤러에서 처리하는 모든 요청의 공통 URL을 지정함.
public class UserController implements UserApi {

  private final UserService userService; //사용자 관련 비즈니스 로직을 처리하는 메서드
  private final UserStatusService userStatusService; //사용자 상태 관련 비즈니스 로직을 처리하는 서비스

  //새로운 사용자를 생성하는 요청을 처리하는 메서드
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<User> create(
      @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest, //사용자 생성 정보를 전달받음.
      @RequestPart(value = "profile", required = false) MultipartFile profile
      //프로필 이미지를 전달받음.(프로필 이미지가 없어도 요청가능)
  ) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(
            profile) //전달 받은 MultipartFile을 BinaryContentCreateRequest로 변환함.
        .flatMap(this::resolveProfileRequest);
    User createdUser = userService.create(userCreateRequest, profileRequest);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(createdUser);
  }

  //기존 사용자 정보를 수정하는 요청을 처리하는 메서드
  @PatchMapping(
      value = "/{userId}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<User> update(
      @PathVariable UUID userId, //수정한 사용자의 UUID를 전달받음.
      @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest, //수정할 사용자 정보를 전달받음.
      @RequestParam(value = "profile", required = false) MultipartFile profile //수정할 프로필 이미지를 전달받음.
  ) {
    Optional<BinaryContentCreateRequest> profileRequest = Optional.ofNullable(
            profile) //프로필 이미지를 BinaryContentCreateRequest로 변환함.
        .flatMap(this::resolveProfileRequest);
    User updatedUser = userService.update(userId, userUpdateRequest, profileRequest);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUser);
  }

  @DeleteMapping("/{userId}") //사용자를 삭제하는 요청을 처리하는 메서드
  public ResponseEntity<Void> delete(@PathVariable("userId") UUID userId) {
    userService.delete(userId);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping //모든 사용자 목록을 조회하는 요청을 처리하는 메서드
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> users = userService.findAll();
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(users);
  }

  @PatchMapping(path = "/{userId}/userStatus") //특정 사용자의 상태를 수정하는 요청을 처리하는 메서드
  public ResponseEntity<UserStatus> updateUserStatusByUserId(@PathVariable("userId") UUID userId,
      @RequestBody UserStatusUpdateRequest request) {
    UserStatus updatedUserStatus = userStatusService.updateByUserId(userId, request);
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(updatedUserStatus);
  }

  //MultipartFile 형태의 프로필 이미지를 BinaryContentCreateRequest 객체로 변환하는 내부 메서드
  private Optional<BinaryContentCreateRequest> resolveProfileRequest(MultipartFile profileFile) {
    if (profileFile.isEmpty()) { //전달받은 파일이 비어 있는 경우. Optional.empty()를 반환하여 프로필이 없음을 나타냄.
      return Optional.empty();
    } else {
      try {
        BinaryContentCreateRequest binaryContentCreateRequest = new BinaryContentCreateRequest(
            profileFile.getOriginalFilename(),
            profileFile.getContentType(),
            profileFile.getBytes()
        );
        return Optional.of(binaryContentCreateRequest);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
