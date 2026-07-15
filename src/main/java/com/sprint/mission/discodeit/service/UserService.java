package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.Collection;
import java.util.UUID;

public interface UserService {
	UserDto create(UserCreateRequest createRequest);
	UserDto findById(UUID id);
	Collection<UserDto> findAll();
	UserDto update(UUID userId, UserUpdateRequest updateRequest);
	void delete(UUID id);
	
}
