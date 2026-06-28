package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.request.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
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
import org.springframework.web.bind.annotation.RequestPart;

@Tag(name = "Channel", description = "Channel API")
@RequestMapping("/api/channels")
public interface ChannelApi {

  @Operation(summary = "Public Channel")
  @PostMapping("/public")
  ResponseEntity<Channel> create(@RequestBody PublicChannelCreateRequest request);

  @Operation(summary = "Private Channel")
  @PostMapping("/private")
  ResponseEntity<Channel> create(@RequestBody PrivateChannelCreateRequest request);

  @Operation(summary = "Channel 정보 수정")
  @PostMapping("/channelId")
  ResponseEntity<Channel> update(
      @PathVariable UUID channelId,
      @RequestBody PublicChannelUpdateRequest request
  );

  @Operation(summary = "Channel 삭제")
  @PostMapping("/channelId")
  ResponseEntity<Void> delete(@PathVariable UUID channelId);

  @Operation(summary = "User가 참여 중인 Channel 목록 조회")
  @GetMapping
  ResponseEntity<List<ChannelDto>> findAll(@RequestParam UUID userId);
}
