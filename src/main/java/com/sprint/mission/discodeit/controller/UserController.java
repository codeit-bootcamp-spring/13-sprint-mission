package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService userService;

    //사용자 등록
    @Operation(summary = "User 등록")
    @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<UserDto> createUser(@Valid @RequestPart("userCreateRequest") UserCreateRequest request,
                                           @RequestPart(value = "profile", required = false) MultipartFile profile) {
        log.debug("유저 생성 API 요청");

        UserDto createdUser = userService.createUser(request, profile);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    //모든 사용자를 조회
    @Operation(summary = "전체 User 목록 조회")
    @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> responseList = userService.getUsers();

        return ResponseEntity.ok().body(responseList);
    }

    //사용자 정보 수정
    @Operation(summary = "User 정보 수정")
    @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨")
    @RequestMapping(
            value = "/{userId}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public ResponseEntity<UserDto> updateUser(@Parameter(description = "수정할 User ID", required = true)
                                           @PathVariable UUID userId,
                                           @Valid @RequestPart("userUpdateRequest") UserUpdateRequest request,
                                           @RequestPart(value = "profile", required = false) MultipartFile profile) {
        log.debug("유저 수정 API 요청");

        UserDto response = userService.updateUser(userId, request, profile);

        return ResponseEntity.ok().body(response);
    }

    //사용자 삭제
    @Operation(summary = "User 삭제")
    @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨")
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteUser(@Parameter(description = "삭제할 User ID", required = true)
                                           @PathVariable UUID userId) {
        log.debug("유저 삭제 API 요청");

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }

}
