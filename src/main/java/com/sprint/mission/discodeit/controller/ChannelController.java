package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  @RequestMapping(value = "/public", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPublic(
      @RequestBody PublicChannelCreateRequest publicChannelCreateRequest) {
    ChannelDto channelPublic = channelService.createPublic(publicChannelCreateRequest.name(),
        publicChannelCreateRequest.description());
    return ResponseEntity.status(HttpStatus.CREATED).body(channelPublic);
  }

  @RequestMapping(value = "/private", method = RequestMethod.POST)
  public ResponseEntity<ChannelDto> createPrivate(
      @RequestBody PrivateChannelCreateRequest privateChannelCreateRequest) {
    ChannelDto channelPrivate = channelService.createPrivate(
        privateChannelCreateRequest.participantIds());
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
    ChannelDto updateChannel = channelService.update(channelId, channelUpdateRequest.newName(),
        channelUpdateRequest.newDescription());
    return ResponseEntity.status(HttpStatus.OK).body(updateChannel);
  }

  @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
