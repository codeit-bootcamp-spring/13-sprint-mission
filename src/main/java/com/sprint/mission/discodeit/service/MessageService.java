package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface MessageService {

	MessageDto create(MessageCreateRequest request);
	MessageDto findById(UUID id);
	Collection<MessageDto> findAllByChannelId(UUID channelId);
	MessageDto update(UUID messageId, MessageUpdateRequest request);
	void delete(UUID id);

}
