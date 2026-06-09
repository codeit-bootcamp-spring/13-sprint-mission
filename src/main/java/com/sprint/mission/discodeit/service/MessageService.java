package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponse create(MessageRequest.CreateMessageRequest request);

    MessageResponse find(UUID messageId);

    List<MessageResponse> findAllByChannelId(UUID channelId);

    MessageResponse update(UUID id, MessageRequest.UpdateMessageRequest request);

    void delete(UUID messageId);
}
