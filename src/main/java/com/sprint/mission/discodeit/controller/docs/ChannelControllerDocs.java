package com.sprint.mission.discodeit.controller.docs;


import com.sprint.mission.discodeit.dto.channel.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Channel", description = "Channel API")
public interface ChannelControllerDocs {

    @Operation(summary = "Public 채널 생성 API")
    @PostMapping( "/public")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ChannelDto> createChannel(@RequestBody PublicChannelRequest publicRequest);

    @Operation(summary = "Private 채널 생성 API")
    @PostMapping("/private")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ChannelDto> createChannel(@RequestBody PrivateChannelRequest privateRequest);

    @Operation(summary = "Public 채널 수정 API")
    @PatchMapping("/{channelId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<ChannelDto> updateChannel(@PathVariable UUID channelId,
                                                         @RequestBody ChannelUpdateRequest request);

    @Operation(summary = "채널 삭제 API")
    @DeleteMapping("/{channelId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId);

    @Operation(summary = "특정 사용자 채널 목록 조회 API")
    @GetMapping()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<List<ChannelDto>> findAllChannel(@RequestParam UUID userId);

}
