package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
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

public interface MessageControllerDoc {


    @Operation(summary = "다수 조회", description = "해당 유저가 볼 수 있는 모든 메세지 조회")
    @ApiResponses(
            @ApiResponse(responseCode = "200",description = "조회 성공")
    )
    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<Message>> findMessageByChannel(
            @RequestParam(value = "channelId") UUID channelId
    );


    @Operation(summary = "매세지 생성", description = "메세지 생성")
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
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    ResponseEntity<Message> create(
            @Parameter(content = @Content(mediaType = "application/json"))
            @RequestPart(value = "messageCreateRequest")
            MessageCreateRequest mcr,
            @RequestPart(value = "attachments", required = false)
            List<MultipartFile> att
    );

    @Operation(summary = "메세지 수정", description = "특정 메세지 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메세지 업데이트 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 메세지 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    ResponseEntity<Message> modifyMessage(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest msi
    );

    @Operation(summary = "메세지 삭제", description = "해당 메세지 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 메세지 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId
    );

}
