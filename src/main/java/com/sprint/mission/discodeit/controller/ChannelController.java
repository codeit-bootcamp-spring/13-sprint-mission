package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@ResponseBody
@RequestMapping("/api/channels")
public class ChannelController {

  private final ChannelService channelService;

  // 공개 채널 생성
  @RequestMapping(
      value = "/public",
      method = RequestMethod.POST
  )
  public Object createPublicChannel(
      @RequestBody PublicChannelCreateRequest request
  ) {

    return channelService.create(request);
  }

  // 비공개 채널 생성
  @RequestMapping(
      value = "/private",
      method = RequestMethod.POST
  )
  public Object createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest request
  ) {

    return channelService.create(request);
  }

  // 공개 채널 수정
  @RequestMapping(
      value = "/{channelId}",
      method = RequestMethod.PATCH
  )
  public Object updateChannel(
      @PathVariable UUID channelId,
      @RequestBody PublicChannelUpdateRequest request
  ) {

    return channelService.update(
        channelId,
        request
    );
  }

  // 채널 삭제
  @RequestMapping(
      value = "/{channelId}",
      method = RequestMethod.DELETE
  )
  public void deleteChannel(
      @PathVariable UUID channelId
  ) {

    channelService.delete(channelId);
  }

  // 특정 사용자가 볼 수 있는 채널 목록 조회
  @RequestMapping(method = RequestMethod.GET)
  public Object getChannelsByUser(
      @RequestParam UUID userId
  ) {

    return channelService.findAllByUserId(userId);
  }
}