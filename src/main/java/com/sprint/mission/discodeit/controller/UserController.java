package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") // 컨트롤러에 공통 url 매핑, 기본 시작을 지정
public class UserController {
    // Controller -> Service -> Repository
    private final UserService userService;
    private final UserStatusService userStatusService;
    // 의존성 관계 추가

    @RequestMapping("/hello")
    public String hello(){
        return "home";
    }

    // 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request){
        UserResponse user=userService.create(request);
        URI location=URI.create("/api/users/"+user.getId());
        return ResponseEntity.created(location).body(user);
    }

    // 사용자 정보 수정
    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<UserResponse> update(@PathVariable UUID userId,
                                               @Valid @RequestBody UserUpdateRequest request){
        UserResponse updatedUser = userService.update(userId, request);
        return ResponseEntity.ok().body(updatedUser);
    }

    // 사용자 삭제
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID userId){
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }

    // 모든 사용자 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> findAll(){
        List<UserResponse> foundUsers = userService.findAll();
        return ResponseEntity.ok().body(foundUsers);
    }

    // 사용자의 온라인 상태 업데이트
    @RequestMapping(value = "/{userId}/online-status", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusResponse> updateOnlineStatus(@PathVariable UUID userId){
        UserStatusResponse updatedStatus = userStatusService.updateByUserId(userId);
        return ResponseEntity.ok().body(updatedStatus);
    }
}
