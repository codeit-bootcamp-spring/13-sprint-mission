package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto create(UserCreateRequest request);

    UserDto find(UUID userId);

    List<UserDto> findAll();

    UserDto update(UserUpdateRequest request);

    void delete(UUID userId);
}
