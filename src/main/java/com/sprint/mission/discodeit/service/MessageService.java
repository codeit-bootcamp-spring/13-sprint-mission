package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface MessageService {

	Message create(String content, UUID channelId, UUID userId);
	Message findById(UUID id);
	Collection<Message> findAll();
	void update(UUID id, String content);
	void delete(UUID id);
	

}
