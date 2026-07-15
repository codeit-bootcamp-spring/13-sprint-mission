package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.ChannelApi;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/channels")
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  @PostMapping(path = "public")
  public ResponseEntity<Channel> create(@RequestBody PublicChannelCreateRequest request) {
    // Request DTO에서 필요한 값만 Service에 전달
    Channel createdChannel = channelService.create(
            request.name(),        // String만 꺼내서 전달
            request.description()  // String만 꺼내서 전달
    );
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdChannel);
  }

  @PostMapping(path = "private")
  public ResponseEntity<Channel> create(@RequestBody PrivateChannelCreateRequest request) {
    // 참여자 ID 목록만 꺼내서 전달
    Channel createdChannel = channelService.create(
            request.participantIds()
    );
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdChannel);
  }

  @PatchMapping(path = "{channelId}")
  public ResponseEntity<Channel> update(
          @PathVariable("channelId") UUID channelId,
          @RequestBody PublicChannelUpdateRequest request) {
    // 수정할 값만 꺼내서 전달
    Channel updatedChannel = channelService.update(
            channelId,
            request.newName(),        // String만 꺼내서 전달
            request.newDescription()  // String만 꺼내서 전달
    );
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedChannel);
  }

  @DeleteMapping(path = "{channelId}")
  public ResponseEntity<Void> delete(@PathVariable("channelId") UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }

  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAll(@RequestParam("userId") UUID userId) {
    List<ChannelDto> channels = channelService.findAllByUserId(userId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(channels);
  }
}