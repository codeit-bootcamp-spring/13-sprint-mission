package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.CreateBinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponse create(CreateMessageRequest request);

    MessageResponse find(UUID id);

    List<MessageResponse> findAllByChannelId(UUID channelId);

    MessageResponse update(UUID id, UpdateMessageRequest request);

    void delete(UUID id);
    
}
