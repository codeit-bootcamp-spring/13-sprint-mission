package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class JcfUserService implements UserService {
	@Override
	public void create(User user) {
	
	}
	
	@Override
	public User findById(UUID id) {
		return null;
	}
	
	@Override
	public Collection<User> findAll() {
		return List.of();
	}
	
	@Override
	public void update(UUID id, String name, String email) {
	
	}
	
	@Override
	public void delete(UUID id) {
	
	}
}
