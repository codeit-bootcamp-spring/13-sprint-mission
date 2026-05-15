package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFMessageService implements MessageService {
	
	private final Map<UUID, Message> message;
	public JCFMessageService() {
		this.message = new ConcurrentHashMap<UUID, Message>();
	}
	
	
	@Override
	public void create(Message message) {
		this.message.put(message.getId(), message);
	}
	
	@Override
	public Message findById(UUID id) {
		return message.get(id);
	}
	
	@Override
	public Collection<Message> findAll() {
		return message.values();
	}
	
	@Override
	public void update(UUID id, String content) {
		Message message = this.message.get(id);
		if (message != null) {
			message.update(content);
		}
	}
	
	@Override
	public void delete(UUID id) {
		message.remove(id);
	}
}
