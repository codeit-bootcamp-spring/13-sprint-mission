package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto create(UserCreateRequest request);

    UserDto find(UUID id);

    User findById(UUID id);

    List<UserDto> findAll();

    UserDto update(UserUpdateRequest request);

    void delete(UUID id);
}
