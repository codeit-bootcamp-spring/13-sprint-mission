package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.Collection;
import java.util.UUID;

public interface UserService {
	UserResponse create(UserCreateRequest createRequest);
	UserResponse findById(UUID id);
	Collection<UserResponse> findAll();
	Collection<UserDto> findAllDto();
	UserResponse update(UserUpdateRequest updateRequest);
	void delete(UUID id);
	
}
