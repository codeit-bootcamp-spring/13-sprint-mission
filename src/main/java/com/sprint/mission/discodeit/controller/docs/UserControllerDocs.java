package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "User", description = "User API")
public interface UserControllerDocs {

    @PostMapping
    @Operation(summary = "User 생성 API")
    public ResponseEntity<UserResponse> createUser(@RequestPart UserCreateRequest userCreateRequest,
                                                   @RequestPart(required = false) MultipartFile profile);

    @Operation(summary = "User 수정 API")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID userId,
                                                   @RequestPart UserUpdateRequest userUpdateRequest,
                                                   @RequestPart(required = false) MultipartFile profile);

    @Operation(summary = "User 삭제 API")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId);

    @Operation(summary = "User 단건 조회 API")
    public ResponseEntity<UserResponse> findUser (@PathVariable UUID userId);

    @Operation(summary = "전체 User 조회 API")
    public ResponseEntity<List<UserResponse>> findAllUser();

    @Operation(summary = "User 온라인 상태 업데이트 API")
    public ResponseEntity<UserStatusResponse> updateUserStatus(@PathVariable UUID userId,
                                                               @RequestBody UserStatusUpdateRequest request);

}
