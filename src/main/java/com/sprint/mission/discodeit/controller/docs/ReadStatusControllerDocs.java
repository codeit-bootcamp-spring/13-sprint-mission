package com.sprint.mission.discodeit.controller.docs;


import com.sprint.mission.discodeit.dto.readstatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusResponse;
import com.sprint.mission.discodeit.dto.readstatus.ReadStatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Tag(name = "ReadStatus", description = "Message 읽음 상태 API")
public interface ReadStatusControllerDocs {

    @Operation(summary = "Message 읽음 상태 생성 API")
    @PostMapping()
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ReadStatusDto> createReadStatus(
            @RequestBody ReadStatusCreateRequest request);

    @Operation(summary = "Message 읽음 상태 수정 API")
    @PatchMapping( "/{readStatusId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<ReadStatusDto> ReadStatusUpdate(
            @PathVariable UUID readStatusId, @RequestBody ReadStatusUpdateRequest request);

    @Operation(summary = "User Message 읽음 상태 목록 조회 API")
    @GetMapping()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<List<ReadStatusDto>> getReadStatus(@RequestParam UUID userId);
}
