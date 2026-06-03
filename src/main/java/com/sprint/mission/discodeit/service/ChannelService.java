package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface ChannelService {
	
	Channel create(String name, String description, User creator, ChannelType type);
	Channel findById(UUID id);
	Collection<Channel> findAll();
	void update(UUID id, String name, String description);
	void delete(UUID id);
	
}
