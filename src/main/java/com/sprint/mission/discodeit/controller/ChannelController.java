package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  @RequestMapping(value = "/public", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPublic(
      @RequestBody @Valid PublicChannelCreateRequest publicChannelCreateRequest) {
    log.debug("[Public 채널 생성 요청] name: {}", publicChannelCreateRequest.name());

    ChannelDto channelPublic = channelService.createPublic(publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description());

    log.info("[Public 채널 생성 완료] channelId: {}", channelPublic.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(channelPublic);
  }

  @RequestMapping(value = "/private", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPrivate(
      @RequestBody @Valid PrivateChannelCreateRequest privateChannelCreateRequest) {
    log.debug("[Private 채널 생성 요청] participantIds 수: {}",
        privateChannelCreateRequest.participantIds().size());

    ChannelDto channelPrivate = channelService.createPrivate(
        privateChannelCreateRequest.participantIds());

    log.info("[Private 채널 생성 완료] channelId: {}", channelPrivate.id());
    return ResponseEntity.status(HttpStatus.CREATED).body((channelPrivate));
  }

  @RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
  public ResponseEntity<ChannelDto> find(@PathVariable UUID channelId) {
    ChannelDto channel = channelService.find(channelId);
    return ResponseEntity.status(HttpStatus.OK).body(channel);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<ChannelDto>> findAllByUserId(@RequestParam UUID userId) {
    List<ChannelDto> allChannel = channelService.findAllByUserId(userId);
    return ResponseEntity.status(HttpStatus.OK).body(allChannel);
  }

  @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
  public ResponseEntity<ChannelDto> update(@PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequest channelUpdateRequest) {
    log.debug("[채널 수정 요청] channelId: {}", channelId);

    ChannelDto updateChannel = channelService.update(channelId, channelUpdateRequest.newName(),
        channelUpdateRequest.newDescription());

    log.info("[채널 수정 완료] channelId: {}", updateChannel.id());
    return ResponseEntity.status(HttpStatus.OK).body(updateChannel);
  }

  @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    log.debug("[채널 삭제 요청] channelId: {}", channelId);

    channelService.delete(channelId);
    
    log.info("[채널 삭제 완료] channelId: {}", channelId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}