package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageService {

	MessageDto create(MessageCreateRequest request);
	MessageDto findById(UUID id);
	PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable) ;
	MessageDto update(UUID messageId, MessageUpdateRequest request);
	void delete(UUID id);

}
