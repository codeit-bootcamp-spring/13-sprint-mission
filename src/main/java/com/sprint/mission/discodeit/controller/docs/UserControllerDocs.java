package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<UserDto> createUser(@Valid @RequestPart UserCreateRequest userCreateRequest,
                                              @RequestPart(required = false) MultipartFile profile);

    @Operation(summary = "User 수정 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId,
                                                   @Valid @RequestPart UserUpdateRequest userUpdateRequest,
                                                   @RequestPart(required = false) MultipartFile profile);

    @Operation(summary = "User 삭제 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId);

    @Operation(summary = "User 단건 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<UserDto> findUser (@PathVariable UUID userId);

    @Operation(summary = "전체 User 조회 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<List<UserDto>> findAllUser();

    @Operation(summary = "User 온라인 상태 업데이트 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<UserStatusDto> updateUserStatus(@PathVariable UUID userId,
                                                          @Valid @RequestBody UserStatusUpdateRequest request);

}
