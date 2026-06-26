package com.sprint.mission.discodeit.controller.docs;


import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Channel", description = "Channel API")
public interface ChannelControllerDocs {

    @Operation(summary = "Public 채널 생성 API")
    @PostMapping( "/public")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PublicChannelRequest publicRequest);

    @Operation(summary = "Private 채널 생성 API")
    @PostMapping("/private")
    public ResponseEntity<ChannelResponse> createChannel(@RequestBody PrivateChannelRequest privateRequest);

    @Operation(summary = "Public 채널 수정 API")
    @PatchMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable UUID channelId,
                                                         @RequestBody ChannelUpdateRequest request);

    @Operation(summary = "채널 삭제 API")
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable UUID channelId);

    @Operation(summary = "특정 사용자 채널 목록 조회 API")
    @GetMapping()
    public ResponseEntity<List<ChannelResponse>> findAllChannel(@RequestParam UUID userId);

}
