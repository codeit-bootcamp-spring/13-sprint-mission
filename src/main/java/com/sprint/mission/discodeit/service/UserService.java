package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

  UserDto create(String username, String email, String password, UUID profileId);

  UserDto find(UUID userId);

  List<UserDto> findAll();

  UserDto update(UUID userId, String newUsername, String newEmail, String newPassword,
      UUID newProfileId);

  void delete(UUID userId);
}