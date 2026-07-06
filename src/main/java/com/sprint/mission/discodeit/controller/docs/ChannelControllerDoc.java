package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

public interface ChannelControllerDoc {

    @Operation(summary = "다수 조회", description = "해당 유저가 볼 수 있는 모든 채널 조회")
    @ApiResponses(
            @ApiResponse(responseCode = "200",description = "조회 성공")
    )
    @RequestMapping(value = "", method = RequestMethod.GET)
    ResponseEntity<List<ChannelDto>> findAll(
            @RequestParam(value = "userId") UUID userId
    );

    @Operation(summary = "채널 삭제", description = "해당 id 의 채널 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 채널 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{channelId}",method = RequestMethod.DELETE)
    ResponseEntity<Void> delete(
            @PathVariable UUID channelId
    );

    @Operation(summary = "채널 업데이트", description = "(공개된)채널 정보 업데이트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채널 업데이트 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "비공개 채널은 업데이트 불가",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 채널 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{channelId}",method = RequestMethod.PATCH)
    ResponseEntity<ChannelDto> update(
            @PathVariable UUID channelId,
            @RequestBody PublicChannelUpdateRequest pcur
    );


    @Operation(summary = "비공개 채널 생성", description = "비공개 채널 생성")
    @ApiResponses(
            @ApiResponse(responseCode = "201",description = "생성 완료")
    )
    @RequestMapping(value = "/private",method = RequestMethod.POST)
    ResponseEntity<ChannelDto> createPrivate(
            @RequestBody PrivateChannelCreateRequest cpi
    );

    @Operation(summary = "공개 채널 생성", description = "공개 채널 생성")
    @ApiResponses(
            @ApiResponse(responseCode = "201",description = "생성 완료")
    )
    @RequestMapping(value = "/public",method = RequestMethod.POST)
    ResponseEntity<ChannelDto> createPublic(
            @RequestBody PublicChannelCreateRequest cpi
    );

}
