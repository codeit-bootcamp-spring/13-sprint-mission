package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

// Todo - 에러 핸들러 문서 재작성
public interface UserControllerDoc {

    @Operation(summary = "다수 조회", description = "모든 유저 조회")
    @ApiResponses(
            @ApiResponse(responseCode = "200",description = "조회 성공")
    )
    @RequestMapping(value = "",method = RequestMethod.GET)
    ResponseEntity<List<UserDto>> findAll();


    @Operation(summary = "유저 생성", description = "유저 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저 / 채널 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    )
    ResponseEntity<UserDto> create(
            @Parameter(
                    content = @Content(mediaType = "application/json")
            ) @RequestPart("userCreateRequest") UserCreateRequest uci,
            @RequestPart(value = "profile", required = false) MultipartFile tmb
    );


    @Operation(summary = "유저 삭제", description = "해당 유저 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{userId}",method = RequestMethod.DELETE)
    ResponseEntity<Object> delete(
            @PathVariable UUID userId
    );


    @Operation(summary = "유저 수정", description = "특정 유저 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 업데이트 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(
            value = "/{userId}",
            method = RequestMethod.PATCH,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    ResponseEntity<UserDto> update(
            @PathVariable
            UUID userId,

            @Parameter(content = @Content(mediaType = "application/json"))
            @RequestPart("userUpdateRequest")
            UserUpdateRequest uui,

            @RequestPart (value = "profile", required = false)
            MultipartFile tmb
    );


    @Operation(summary = "유저 상태 수정", description = "특정 유저 상태 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 상태 업데이트 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 유저 상태 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{userId}/userStatus", method = RequestMethod.PATCH)
    ResponseEntity<UserStatusDto> updateUserStatusByUserId(
            @PathVariable UUID userId,
            @RequestBody UserStatusUpdateRequest usur
    );
}
