package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// @Controller + @ResponseBody: 빈등록, 컨트롤러 명시, 모든 메서드 자동 @ResponseBody 적용
@RestController
@RequiredArgsConstructor// LomBok어노테이션, final필드를 매개변수로 바든 생성자 자동 생성
@RequestMapping("/api/users")// 메서드의 URL경로에 URL prefix (/api/users)자동 생성->메서드에는 경로 변수(Path Variable)만 씀.
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    @RequestMapping(method = RequestMethod.POST)   //┌> Dto를 인수로 넣음
    public ResponseEntity<User> create(@RequestBody UserCreateRequest request) {
                                          //└> @RequestBody JSON -> Java 객체로
        User user = userService.create(  //주입 받은 객체
                request.username(), // (@RequestBody UserCreateRequest의 Get은 변수.record의 필드명
                request.email(),    //record에서 값을 가져오는 건 getter가 아니라 접근자 메서드(accessor method)라고 한다.
                request.password(),
                request.profileId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }                                                //└>새러운 리소스 추가
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

    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<User> update(@PathVariable UUID userId, @RequestBody UserUpdateRequest userUpdateRequest) {
        User newUser = userService.update(userId, userUpdateRequest.newUsername(),
                userUpdateRequest.newEmail(),
                userUpdateRequest.newPassword(),
                userUpdateRequest.newProfileId());
        return ResponseEntity.status(HttpStatus.OK).body(newUser);
    }                                                 //└> 리소스 수정 200 요청 성공

    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatus> updateByUserId(@PathVariable UUID userId,
                                                     @RequestBody UserStatusUpdateRequest userStatusUpdateRequest) {
        UserStatus userStatus = userStatusService.updateByUserId(userId, userStatusUpdateRequest );
          // DTO를 통째로 서비스에 전달
             // 서비스 내부에서 request.newLastActiveAt()을 꺼내 UserStatus 엔티티의
              // lastActiveAt 필드를 수정 후 저장소에 저장 -> 각각의 필드 꺼낼 필요 없음.
        return ResponseEntity.status(HttpStatus.OK).body(userStatus);
    }



}
