package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.readstatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
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

public interface ReadStatusControllerDoc {

    @Operation(summary = "다수 조회", description = "해당 유저가 볼 수 있는 모든 readstatus 조회")
    @ApiResponses(
            @ApiResponse(responseCode = "200",description = "조회 성공")
    )
    @RequestMapping(
            value = "",
            method = RequestMethod.GET
    )
    ResponseEntity<List<ReadStatusDto>> findAllByUserId(
            @RequestParam(value = "userId") UUID userId
    );

    @Operation(summary = "상태 생성", description = "읽음 상태 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 존재함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
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
            method = RequestMethod.POST
    )
    ResponseEntity<ReadStatusDto> create(
            @RequestBody ReadStatusCreateRequest rscr
    );

    @Operation(summary = "상태 업데이트", description = "현재 상태로 업데이트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업데이트 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 상태 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/{readStatusId}", method = RequestMethod.PATCH)
    ResponseEntity<ReadStatusDto> update(
            @PathVariable UUID readStatusId,
            @RequestBody ReadStatusUpdateRequest rsur
    );

}
