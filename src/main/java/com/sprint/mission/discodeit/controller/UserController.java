package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

// @Controller + @ResponseBody: 빈등록, 컨트롤러 명시, 모든 메서드 자동 @ResponseBody 적용
@Slf4j
@RestController
@RequiredArgsConstructor// LomBok어노테이션, final필드를 매개변수로 바든 생성자 자동 생성
@RequestMapping("/api/users")
// 메서드의 URL경로에 URL prefix (/api/users)자동 생성->메서드에는 경로 변수(Path Variable)만 씀.
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;
  private final BinaryContentService binaryContentService;

  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      @RequestPart("userCreateRequest") @Valid UserCreateRequest request) {
    //└> @RequestBody JSON -> Java 객체로              //└> Dto를 인수로 넣음
    log.debug("[User 생성 요청] username: {}, email: {}, profile 첨부 여부: {}",
        request.username(), request.email(), profile != null);
    BinaryContentDto content = null;
    if (profile != null) {
      try {
        content = binaryContentService.create(profile.getOriginalFilename(),
            profile.getSize(), profile.getContentType(),
            profile.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    UserDto userDto = userService.create(  //주입 받은 객체
        request.username(), // (@RequestBody UserCreateRequest의 Get은 변수.record의 필드명
        request.email(),    //record에서 값을 가져오는 건 getter가 아니라 접근자 메서드(accessor method)라고 한다.
        request.password(),
        Optional.ofNullable(content)
            .map(BinaryContentDto::id)
            .orElse(null)
        //Optional-> 단일 객체 Optional로 변환시 사용/ stream은 복수 객체의 타입을 변환시 사용
        //ofNullable() -> 객체를 Optional로 감쌈.(null이여도 감싼다.)
        //Optional에 map: 위에서 Optional로 감싼게 null이면 map 스킵,
        // 값이 있으면 Optional로 감싼 객체의id를 꺼내서 Optional로 감싸서 반환.
        //.orElse(null) -> 옵셔널로 감싼 값(id)을 다시 꺼내서 반환
        //-> ofNullable에서 null이였으면 map 스킵되고 바로 orElse(null)에서 null을 꺼내서 반환.
    );
    log.info("[User 생성 완료] userId: {}", userDto.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
  }                                                //└>새로운 리소스 추가

  //HttpStatus.CREATED 201 리소스 생성
  @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
  public ResponseEntity<UserDto> find(@PathVariable UUID userId) {
    UserDto user = userService.find(userId); //└> URL의 {Id} 값을 여기서 받음
    return ResponseEntity.status(HttpStatus.OK).body(user);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> userAll = userService.findAll();
    return ResponseEntity.status(HttpStatus.OK).body(userAll);
  }

  @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      //MultipartFile: Spring이 파일 데이터를 다루기 위해 제공하는 타입
      //getBytes(), getSize(), getContentType() 같은 메서드로 파일 정보를 꺼낼 수 있음.
      //프론트에서 요구하는 방식이라 일단 추가. 415오류 해결.
      @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") @Valid UserUpdateRequest userUpdateRequest) {
    log.debug("[User 수정 요청] userId: {}, profile 첨부 여부: {}", userId, profile != null);

    BinaryContentDto content = null;
    if (profile != null) {
      try {
        content = binaryContentService.create(profile.getOriginalFilename(),
            profile.getSize(), profile.getContentType(), profile.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);//전역으로 처리할까 했지만 다음 미션 DB연동이라 생략.
      }
    }

    UserDto newUser = userService.update(userId, userUpdateRequest.newUsername(),
        userUpdateRequest.newEmail(),
        userUpdateRequest.newPassword(),
        content != null ? content.id() : userUpdateRequest.newProfileId()
    );
    log.info("[User 수정 완료] userId: {}", newUser.id());
    return ResponseEntity.status(HttpStatus.OK).body(newUser);
  }                                                 //└> 리소스 수정 200 요청 성공

  @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID userId) {
    log.debug("[User 삭제 요청] userId: {}", userId);

    userService.delete(userId);

    log.info("[User 삭제 완료] userId: {}", userId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
  public ResponseEntity<UserDto> updateByUserId(@PathVariable UUID userId) {
    log.debug("[UserStatus 수정 요청] userId: {}", userId);

    UserDto userStatus = userStatusService.updateToNowByUserId(userId);

    log.info("[UserStatus 수정 완료] userId: {}", userId);
    return ResponseEntity.status(HttpStatus.OK).body(userStatus);
  }


}