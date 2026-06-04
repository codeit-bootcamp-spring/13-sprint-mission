package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFMessageService implements MessageService {
	
	private final Map<UUID, Message> message;
	private final UserService userService;
	private final ChannelService channelService;
	
	public JCFMessageService(UserService userService, ChannelService channelService) {
		this.userService = userService;
		this.channelService = channelService;
		this.message = new ConcurrentHashMap<UUID, Message>();
	}
	
	
	@Override
	public void create(Message message) {
		if (userService.findById(message.getAuthor().getId()) == null) {
			throw new IllegalArgumentException("존재하지 않는 유저의 메시지입니다.");
		}
		if (channelService.findById(message.getChannel().getId()) == null) {
			throw new IllegalArgumentException("존재하지 않는 채널의 메시지입니다.");
		}
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
