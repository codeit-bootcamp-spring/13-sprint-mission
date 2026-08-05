package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.util.UUID;

public interface MessageService {

    MessageDto create(CreateMessageRequest request);

    MessageDto find(UUID id);

    PageResponse<MessageDto> findAllByChannelId(
            UUID channelId,
            int page
    );

    MessageDto update(
            UUID id,
            UpdateMessageRequest request
    );

    void delete(UUID id);
}