package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface MessageService {

    MessageDto create(MessageCreateRequest request);

    PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, Pageable pageable);

    MessageDto update(MessageUpdateRequest request);

    void delete(UUID id);
}
