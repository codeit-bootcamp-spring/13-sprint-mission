package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.swagger.ChannelApi;
import com.sprint.mission.discodeit.dto.channel.ChannelPrivateCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelPublicCreateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Slf4j
public class ChannelController implements ChannelApi {

  private final ChannelService channelService;

  @Override
  @PostMapping("/public")
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ResponseEntity<ChannelResponse> createPublicChannel(
      @Valid @RequestBody ChannelPublicCreateRequest request) {
    log.debug("공개 채널 생성 요청: name={}", request.name());
    ChannelResponse response = channelService.createPublicChannel(request);

    log.info("공개 채널 생성 응답: channelId={}", response.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @PostMapping("/private")
  public ResponseEntity<ChannelResponse> createPrivateChannel(
      @Valid @RequestBody ChannelPrivateCreateRequest request) {
    log.debug("비공개 채널 생성 요청: participantCount={}", request.participantIds().size());
    ChannelResponse response = channelService.createPrivateChannel(request);

    log.info("비공개 채널 생성 응답: channelId={}", response.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @PatchMapping("/{channelId}")
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ResponseEntity<ChannelResponse> updateChannel(
      @PathVariable UUID channelId,
      @Valid @RequestBody ChannelUpdateRequest request) {
    log.debug("채널 수정 요청: channelId={}", channelId);
    ChannelResponse response = channelService.updateChannel(channelId, request);

    log.info("채널 수정 응답: channelId={}", channelId);
    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping("/{channelId}")
  @PreAuthorize("hasRole('CHANNEL_MANAGER')")
  public ResponseEntity<Void> deleteChannel(@PathVariable("channelId") UUID channelId) {
    log.debug("채널 삭제 요청: channelId={}", channelId);
    channelService.deleteChannel(channelId);

    log.info("채널 삭제 응답: channelId={}", channelId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @GetMapping
  public ResponseEntity<List<ChannelResponse>> findChannelByUserId(
      @RequestParam("userId") UUID userId) {
    return ResponseEntity.status(HttpStatus.OK).body(channelService.findAllByUserId(userId));
  }
}
