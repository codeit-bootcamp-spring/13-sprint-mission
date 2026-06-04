package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message createMessage(MessageCreateRequest request);
    List<Message> findAllByChannelId(UUID channelId);
    MessageUpdateResponse updateMessage(UUID messageId, MessageUpdateRequest request);
    void deleteMessage(UUID messageId);

}
