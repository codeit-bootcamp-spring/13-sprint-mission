package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Collection;
import java.util.UUID;

public interface MessageService {

	void create(Message message);
	Message findById(UUID id);
	Collection<Message> findAll();
	void update(UUID id, String content);
	void delete(UUID id);
	

}
