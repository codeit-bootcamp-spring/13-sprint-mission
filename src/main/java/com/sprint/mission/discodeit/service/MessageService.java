package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageDto create(MessageCreateRequest request);

    List<MessageDto> findAllByChannelId(UUID channelId);

    MessageDto update(MessageUpdateRequest request);

    void delete(UUID id);
}
