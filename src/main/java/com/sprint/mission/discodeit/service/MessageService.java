package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.util.UUID;

public interface MessageService {

  MessageDto create(MessageCreateRequest request);

  MessageDto find(UUID messageId);

  PageResponse<MessageDto> findAllByChannelId(UUID channelId);

  MessageDto update(UUID messageId, MessageUpdateRequest request);

  void delete(UUID messageId);
}