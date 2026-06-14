package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface MessageService {

	MessageResponse create(MessageCreateRequest request);
	MessageResponse findById(UUID id);
	Collection<MessageResponse> findAllByChannelId(UUID channelId);
	MessageResponse update(MessageUpdateRequest request);
	void delete(UUID id);

}
