package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  @PostMapping("/public")
  public ResponseEntity<ChannelDto> createPublicChannel(
      @RequestBody @Valid PublicChannelCreateRequest request) {
    ChannelDto response = channelService.createPublicChannel(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/private")
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @RequestBody @Valid PrivateChannelCreateRequest request) {
    ChannelDto response = channelService.createPrivateChannel(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto> update(@PathVariable("channelId") UUID id,
      @RequestBody @Valid ChannelUpdateRequest request) {
    ChannelDto response = channelService.update(id, request);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(@PathVariable("channelId") UUID id) {
    channelService.delete(id);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/channelId")
  public ResponseEntity<ChannelDto> findById(@PathVariable("channelId") UUID id) {
    ChannelDto response = channelService.findById(id);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAll(@RequestParam UUID userId) {
    List<ChannelDto> responses = channelService.findAllByUserId(userId);

    return ResponseEntity.ok(responses);
  }

}
