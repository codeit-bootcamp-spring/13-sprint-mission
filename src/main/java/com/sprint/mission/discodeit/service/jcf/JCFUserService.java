package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFUserService implements UserService {
	
	private final Map<UUID, User> data;
	
	public JCFUserService() {
		this.data = new ConcurrentHashMap<UUID, User>();
	}
	
	
	@Override
	public void create(User user) {
		data.put(user.getId(), user);
	}
	
	@Override
	public User findById(UUID id) {
		return data.get(id);
	}
	
	@Override
	public Collection<User> findAll() {
		return data.values();
	}
	
	@Override
	public void update(UUID id, String name, String email) {
		User user = data.get(id);
		if ( user != null) {
			user.renew(name, email);
		}
	}
	
	@Override
	public void delete(UUID id) {
		data.remove(id);
	}
}
