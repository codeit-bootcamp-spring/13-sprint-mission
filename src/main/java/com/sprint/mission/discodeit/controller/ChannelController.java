package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
@Tag(name = "Channel", description = "Channel API")
public class ChannelController {

    private final ChannelService channelService;

    //비공개 채널 생성
    @Operation(summary = "Private Channel 생성")
    @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨")
    @RequestMapping(value = "/private", method = RequestMethod.POST)
    public ResponseEntity<Channel> createPrivateChannel(@Valid @RequestBody PrivateChannelCreateRequest request) {
        Channel createdChannel = channelService.createPrivateChannel(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
    }

    //공개 채널 생성
    @Operation(summary = "Public Channel 생성")
    @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨")
    @RequestMapping(value = "/public", method = RequestMethod.POST)
    public ResponseEntity<Channel> createPublicChannel(@Valid @RequestBody PublicChannelCreateRequest request) {
        Channel createdChannel = channelService.createPublicChannel(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdChannel);
    }

    //특정 사용자가 볼 수 있는 모든 채널 목록 조회
    @Operation(summary = "User가 참여 중인 Channel 목록 조회")
    @ApiResponse(responseCode = "200", description = "Channel 목록 조회 성공")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelFindResponse>> findChannelsByUser(@Parameter(description = "조회할 User ID", required = true)
                                                                        @RequestParam UUID userId) {
        List<ChannelFindResponse> responseList = channelService.findAllByUserId(userId);

        return ResponseEntity.ok().body(responseList);
    }

    //공개 채널의 정보 수정
    @Operation(summary = "Channel 정보 수정")
    @ApiResponse(responseCode = "200", description = "Channel 정보가 성공적으로 수정됨")
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ResponseEntity<Channel> updateChannel(@Parameter(description = "수정할 Channel ID", required = true)
                                                 @PathVariable UUID channelId,
                                                 @Valid @RequestBody PublicChannelUpdateRequest request) {
        Channel response = channelService.updateChannel(channelId, request);

        return ResponseEntity.ok().body(response);
    }

    //채널 삭제
    @Operation(summary = "Channel 삭제")
    @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨")
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteChannel(@Parameter(description = "삭제할 Channel ID", required = true)
                                              @PathVariable UUID channelId) {
        channelService.deleteChannel(channelId);

        return ResponseEntity.noContent().build();
    }

}
