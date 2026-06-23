package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.input.UpdateMessageInput;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    Message createMessage(MessageCreateRequest cmi, Optional<List<BinaryContentCreate>> lbcc);
    List<Message> findallByChannelId(UUID cannelID);
    Message updateMessageData(UUID id, UpdateMessageInput umi);
    void deleteMessage(UUID id);
}
