package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface UserService {
	User create(String name, String email);
	User findById(UUID id);
	Collection<User> findAll();
	void update(UUID id, String name, String email);
	void delete(UUID id);
	
}
