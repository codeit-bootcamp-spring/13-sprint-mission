package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFChannelService implements ChannelService {
	
	private final Map<UUID, Channel> channels;
	public JCFChannelService()
	{
		this.channels = new ConcurrentHashMap<UUID, Channel>();
	}
	
	
	@Override
	public void create(Channel channel) {
		this.channels.put(channel.getId(), channel);
	}
	
	@Override
	public Channel findById(UUID id) {
		return this.channels.get(id);
	}
	
	@Override
	public Collection<Channel> findAll() {
		return channels.values();
	}
	
	@Override
	public void update(UUID id, String name, String description) {
		Channel channel = channels.get(id);
		if ( channel != null) {
			channel.update(name, description);
		}
	}
	
	@Override
	public void delete(UUID id) {
		channels.remove(id);
	}
}
