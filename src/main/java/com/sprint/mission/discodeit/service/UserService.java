package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {

  UserResponse createUser(UserCreateRequest request, MultipartFile profile);

  UserResponse findUserById(UUID id);

  List<UserResponse> findAllUser();

  UserResponse updateUser(UUID userId, UserUpdateRequest request, MultipartFile profile);

  void deleteUser(UUID id);

  UserResponse changeRole(UUID userid, Role role);
}
