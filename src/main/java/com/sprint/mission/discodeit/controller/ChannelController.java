package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Channel", description = "Channel 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  // GET /api/channels?userId=  -> User가 참여 중인 Channel 목록 조회
  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAll(@RequestParam("userId") UUID userId) {
    List<ChannelDto> channels = channelService.findAllByUserId(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(channels);
  }

  // POST /api/channels/public -> Public Channel 생성
  @PostMapping("/public")
  public ResponseEntity<Channel> createPublicChannel(
          @RequestBody PublicChannelCreateRequest request) {
    Channel createdChannel = channelService.create(request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdChannel);
  }

  // POST /api/channels/private -> Private Channel 생성
  @PostMapping("/private")
  public ResponseEntity<Channel> createPrivateChannel(
          @RequestBody PrivateChannelCreateRequest request) {
    Channel createdChannel = channelService.create(request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdChannel);
  }

  // PATCH /api/channels/{channelId} -> Channel 정보 수정
  @PatchMapping("/{channelId}")
  public ResponseEntity<Channel> update(
          @PathVariable("channelId") UUID channelId,
          @RequestBody PublicChannelUpdateRequest request
  ) {
    Channel updatedChannel = channelService.update(channelId, request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedChannel);
  }

  // DELETE /api/channels/{channelId} -> Channel 삭제
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable("channelId") UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }
}