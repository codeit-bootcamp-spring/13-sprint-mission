package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    MessageResponse create(MessageCreateRequest dto);
    Optional<MessageResponse> findById(UUID id);
    List<MessageResponse> findAll(UUID channelId);
    List<MessageResponse> findAllByChannelId(UUID channelId);
    MessageResponse update(UUID id, MessageUpdateRequest dto);
    void delete(UUID id);
}