package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;

import java.util.UUID;

public interface MessageService {

    Message create(CreateMessageRequest request);

    Message find(UUID id);

    PageResponse<MessageDto> findAllByChannelId(
            UUID channelId,
            int page
    );

    Message update(
            UUID id,
            UpdateMessageRequest request
    );

    void delete(UUID id);
}