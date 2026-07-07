package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.docs.UserControllerDocs;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import com.sprint.mission.discodeit.util.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final UserStatusService userStatusService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> createUser(@RequestPart UserCreateRequest userCreateRequest,
                                              @RequestPart(required = false) MultipartFile profile) {
        Optional<BinaryContentCreateRequest> profileRequest = FileUtils.toRequest(profile);
        UserDto userDto = userService.createUser(userCreateRequest, profileRequest.orElse(null));
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }


    @PatchMapping(path = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
                                                   @RequestPart UserUpdateRequest userUpdateRequest,
                                                   @RequestPart(required = false) MultipartFile profile){
        Optional<BinaryContentCreateRequest> profileRequest = FileUtils.toRequest(profile);
        UserDto userDto = userService.updateUser(userId, userUpdateRequest, profileRequest.orElse(null));
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @DeleteMapping( "/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> findUser (@PathVariable UUID userId){
        UserDto userById = userService.findByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userById);
    }

    @GetMapping()
    public ResponseEntity<List<UserDto>> findAllUser(){
        List<UserDto> allUser = userService.findAllUser();
        return ResponseEntity.status(HttpStatus.OK).body(allUser);
    }

    @PatchMapping("/{userId}/userStatus")
    public ResponseEntity<UserStatusDto> updateUserStatus(@PathVariable UUID userId,
                                                          @RequestBody UserStatusUpdateRequest request){
        UserStatusDto userStatusResponse = userStatusService.updateByUserId(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(userStatusResponse);
    }
}
