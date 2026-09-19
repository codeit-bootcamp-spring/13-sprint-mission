package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.FileUploadDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import java.util.List;
import java.util.UUID;

public interface UserService {

  UserDto create(String email, String username, String password, FileUploadDto profile);

  UserDto find(UUID id);

  List<UserDto> findAll();

  UserDto update(UUID id, String newEmail, String newUsername, String newPassword,
      String statusMessage, FileUploadDto profile);

  void delete(UUID id);

  List<UserDto> findAllUsers();

  UserDto updateRole(UUID userId, Role newRole);
}