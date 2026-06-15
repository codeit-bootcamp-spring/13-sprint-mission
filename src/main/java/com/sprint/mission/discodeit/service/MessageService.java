package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.CreateMessageInput;
import com.sprint.mission.discodeit.dto.input.UpdateMessageInput;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    void createMessage(CreateMessageInput cmi);
    List<Message> findallByChannelId(UUID cannelID);
    void updateMessageData(UpdateMessageInput umi);
    void deleteMessage(UUID id);
}
