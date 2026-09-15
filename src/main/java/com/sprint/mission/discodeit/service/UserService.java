package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.command.CreateBinaryContentCommand;
import com.sprint.mission.discodeit.dto.command.CreateUserCommand;
import com.sprint.mission.discodeit.dto.command.UpdateUserCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto create(CreateUserCommand command, CreateBinaryContentCommand profileImage);

    UserDto findByUserId(UUID userId);

    List<UserDto> findAll();

    UserDto update(UUID id, UpdateUserCommand command, CreateBinaryContentCommand profileImage);

    void delete(UUID userId);

    UserDto updateRole(UUID userId, Role newRole);

}
