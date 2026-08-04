package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.mapper.UserMultipartMapper;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/users", "/api/user"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMultipartMapper userMultipartMapper;

    /*
     * 기존 호환용 JSON 사용자 생성
     *
     * API 명세 v1.2 공식 요청은 multipart/form-data 이지만,
     * 기존 Postman 테스트나 기존 코드 호환을 위해 유지한다.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse response = userService.create(request);

        return created(response);
    }

    /*
     * API 명세 v1.2 기준 사용자 생성
     *
     * POST /api/users
     * Content-Type: multipart/form-data
     *
     * parts:
     * - userCreateRequest
     * - profile
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> createWithMultipart(
            @Valid
            @RequestPart("userCreateRequest")
            UserCreateRequest userCreateRequest,

            @RequestPart(value = "profile", required = false)
            MultipartFile profile
    ) throws IOException {
        UserCreateRequest request = userMultipartMapper.toCreateRequest(
                userCreateRequest,
                profile
        );

        UserResponse response = userService.create(request);

        return created(response);
    }

    /*
     * 기존/프론트 호환용 사용자 단건 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> read(
            @PathVariable UUID userId
    ) {
        UserResponse response = userService.read(userId);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     *
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> readAll() {
        List<UserResponse> responses = userService.readAll();

        return ResponseEntity.ok(responses);
    }

    /*
     * 기존 프론트 호환용 목록 조회
     *
     * GET /api/user/findAll
     * GET /api/users/findAll
     */
    @GetMapping("/findAll")
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> responses = userService.readAll();

        return ResponseEntity.ok(responses);
    }

    /*
     * 기존 호환용 JSON 사용자 수정
     */
    @PatchMapping(
            value = "/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserUpdateRequest fixedRequest = new UserUpdateRequest(
                userId,
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getProfileId(),
                request.getProfileImage()
        );

        UserResponse response = userService.update(fixedRequest);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준 사용자 수정
     */
    @PatchMapping(
            value = "/{userId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserResponse> updateWithMultipart(
            @PathVariable UUID userId,

            @Valid
            @RequestPart("userUpdateRequest")
            UserUpdateRequest userUpdateRequest,

            @RequestPart(value = "profile", required = false)
            MultipartFile profile
    ) throws IOException {
        UserUpdateRequest request = userMultipartMapper.toUpdateRequest(
                userId,
                userUpdateRequest,
                profile
        );

        UserResponse response = userService.update(request);

        return ResponseEntity.ok(response);
    }

    /*
     * API 명세 v1.2 기준
     *
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId
    ) {
        userService.delete(userId);

        return ResponseEntity
                .noContent()
                .build();
    }

    private ResponseEntity<UserResponse> created(UserResponse response) {
        URI location = URI.create("/api/users/" + response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}