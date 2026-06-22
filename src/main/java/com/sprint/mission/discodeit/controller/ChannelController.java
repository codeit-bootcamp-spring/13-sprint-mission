package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelListResponse;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  @PostMapping("/public")
  public ResponseEntity<ChannelResponse> createPublicChannel(
      @RequestBody PublicChannelCreateRequest request) {
    ChannelResponse publicResponse = channelService.createPublicChannel(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(publicResponse);
  }

  @PostMapping("/private")
  public ResponseEntity<ChannelResponse> createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest request) {
    ChannelResponse privateResponse = channelService.createPrivateChannel(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(privateResponse);
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelResponse> update(@PathVariable("channelId") UUID id,
      @RequestBody ChannelUpdateRequest request) {
    ChannelResponse response = channelService.update(id, request);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable("channelId") UUID id) {
    channelService.delete(id);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<ChannelListResponse>> findAll(@RequestParam UUID userId) {
    List<ChannelListResponse> responses = channelService.findAllByUserId(userId);

    return ResponseEntity.ok(responses);
  }

}
