package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserFindResponse;
import com.sprint.mission.discodeit.dto.response.UserStatusUpdateResponse;
import com.sprint.mission.discodeit.dto.response.UserUpdateResponse;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    //사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreateRequest request){
        userService.createUser(request);

        return ResponseEntity.ok().build();
    }

    //모든 사용자를 조회
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserFindResponse>> findAll(){
        List<UserFindResponse> responseList= userService.findAllUsers();

        return ResponseEntity.ok().body(responseList);
    }

    //사용자 정보 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request){
        UserUpdateResponse response = userService.updateUser(request);

        return ResponseEntity.ok().body(response);
    }

    //사용자의 온라인 상태 업데이트
    @RequestMapping(value = "/{id}/status", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusUpdateResponse> updateUserStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusUpdateRequest request){
        UserStatusUpdateResponse response = userStatusService.updateUserStatusByUserId(id);

        return ResponseEntity.ok().body(response);
    }

    //사용자 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

}
