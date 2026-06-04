package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.Collection;
import java.util.UUID;

public interface ChannelService {
	
	void create(Channel channel);
	Channel findById(UUID id);
	Collection<Channel> findAll();
	void update(UUID id, String name, String description);
	void delete(UUID id);
	
}
