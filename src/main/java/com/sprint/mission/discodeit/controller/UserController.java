package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import com.sprint.mission.discodeit.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatusService userStatusService;

    //user 생성api
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> createUser(@RequestPart UserCreateRequest request,
                                                   @RequestPart(required = false) MultipartFile profile) {
        Optional<BinaryContentCreateRequest> profileRequest = FileUtils.toRequest(profile);
        UserResponse response = userService.createUser(request, profileRequest.orElse(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    //user 수정 api
    @RequestMapping(value = "/{userid}", method = RequestMethod.PATCH, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID userid,
                                                   @RequestPart UserUpdateRequest request,
                                                   @RequestPart(required = false) MultipartFile profile){
        Optional<BinaryContentCreateRequest> profileRequest = FileUtils.toRequest(profile);
        UserResponse response = userService.updateUser(userid, request, profileRequest.orElse(null));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    //user 삭제 api
    @RequestMapping(value = "/{userid}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userid){
        userService.deleteUser(userid);
        return ResponseEntity.noContent().build();
    }
    //user 단건 조회 api
    @RequestMapping(value = "/{userid}", method = RequestMethod.GET)
    public ResponseEntity<UserResponse> findUser (@PathVariable UUID userid){
        UserResponse userId = userService.findByUserId(userid);
        return ResponseEntity.status(HttpStatus.OK).body(userId);
    }

    //모든 user 조회
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAllUser(){
        List<UserResponse> allUser = userService.findAllUser();
        List<UserDto> dtos = allUser.stream()
                .map(u -> new UserDto(u.id(), u.createdAt(), u.updateAt(), u.name(), u.email(),u.profileId(), u.isOnline()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    //사용자의 온라인 상태를 업데이트
    @RequestMapping(value = "/{userid}/status", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusResponse> updateUserStatus(@PathVariable UUID userid,
                                                               @RequestBody UserStatusUpdateRequest request){
        UserStatusResponse userStatusResponse = userStatusService.updateByUserId(userid, request);
        return ResponseEntity.status(HttpStatus.OK).body(userStatusResponse);
    }
}
