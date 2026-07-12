package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User create(CreateUserRequest request);

    User find(UUID id);

    List<User> findAll();

    User update(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}