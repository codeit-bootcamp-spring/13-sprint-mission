package com.sprint.mission.discodeit.controller.swagger;

import com.sprint.mission.discodeit.dto.request.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "User", description = "User API")
public interface UserApi {

    @Operation(summary = "전체 User 목록 조회")
    @ApiResponse(responseCode = "200", description = "User 목록 조회 성공")
    ResponseEntity<List<UserDto>> list();

    @Operation(
            summary = "User 등록",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserCreateMultipartRequest.class),
                            encoding = {
                                    @Encoding(
                                            name = "userCreateRequest",
                                            contentType = MediaType.APPLICATION_JSON_VALUE
                                    ),
                                    @Encoding(
                                            name = "profile",
                                            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User가 성공적으로 생성됨"),
            @ApiResponse(
                    responseCode = "400",
                    description = "같은 email 또는 username을 사용하는 User가 이미 존재함",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"같은 email 또는 username을 사용하는 User가 이미 존재합니다.\",\"fields\":null}")
                    )
            )
    })
    ResponseEntity<UserDto> create(
            @Parameter(hidden = true)
            @Valid @RequestPart("userCreateRequest") UserCreateRequest userCreateRequest,

            @Parameter(hidden = true)
            @RequestPart(value = "profile", required = false) MultipartFile profile
    );

    @Operation(
            summary = "User 정보 수정",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UserUpdateMultipartRequest.class),
                            encoding = {
                                    @Encoding(
                                            name = "userUpdateRequest",
                                            contentType = MediaType.APPLICATION_JSON_VALUE
                                    ),
                                    @Encoding(
                                            name = "profile",
                                            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨"),
            @ApiResponse(
                    responseCode = "400",
                    description = "같은 email 또는 username을 사용하는 User가 이미 존재함",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"같은 email 또는 username을 사용하는 User가 이미 존재합니다.\",\"fields\":null}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"유저가 존재하지 않습니다.\",\"fields\":null}")
                    )
            )
    })
    ResponseEntity<UserDto> update(
            @Parameter(description = "수정할 User ID", required = true)
            UUID userId,

            @Parameter(hidden = true)
            @Valid @RequestPart("userUpdateRequest") UserUpdateRequest userUpdateRequest,

            @Parameter(hidden = true)
            @RequestPart(value = "profile", required = false) MultipartFile profile
    );


    @Operation(summary = "User 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
            @ApiResponse(
                    responseCode = "404",
                    description = "User를 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\":\"유저가 존재하지 않습니다.\",\"fields\":null}")
                    )
            )
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 User ID", required = true)
            UUID userId);
}
