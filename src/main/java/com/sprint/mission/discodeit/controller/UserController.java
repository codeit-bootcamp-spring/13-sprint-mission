package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;




    //user 생성api
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request){
        UserResponse response = userService.createUser(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    //user 수정 api
    @RequestMapping(value = "/{userid}", method = RequestMethod.PATCH)
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID userid, @RequestBody UserUpdateRequest request){
        UserResponse response = userService.updateUser(userid, request, null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    //user 삭제 api
    @RequestMapping(value = "/{userid}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userid){
        userService.deleteUser(userid);
        return ResponseEntity.noContent().build();
    }
    //모든 user 조회
    @RequestMapping(value = "/findalluser", method = RequestMethod.GET)
    public ResponseEntity<List<UserResponse>> findAllUser(){
        List<UserResponse> allUser = userService.findAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(allUser);
    }

    //사용자의 온라인 상태를 업데이트
    @RequestMapping(value = "/{userid}/status", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusResponse> updateUserStatus(@PathVariable UUID userid,
                                                               @RequestBody UserStatusUpdateRequest request){
        UserStatusResponse userStatusResponse = userStatusService.updateByUserId(userid, request);
        return ResponseEntity.status(HttpStatus.OK).body(userStatusResponse);
    }








}
