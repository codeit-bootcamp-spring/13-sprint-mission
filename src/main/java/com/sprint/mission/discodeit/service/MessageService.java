package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {
    MessageDto createMessage(MessageCreateRequest cmi, Optional<List<BinaryContentCreate>> lbcc);
    PageResponse<MessageDto> findallByChannelId(UUID cannelID, Pageable pageable);
    MessageDto updateMessageData(UUID id, MessageUpdateRequest umi);
    void deleteMessage(UUID id);
}
