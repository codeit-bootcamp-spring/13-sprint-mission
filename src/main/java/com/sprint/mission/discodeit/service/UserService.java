package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserListResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

  UserResponse create(UserCreateRequest request, MultipartFile profile);

  UserResponse findById(UUID userId);

  List<UserListResponse> findAll();

  UserResponse update(UUID id, UserUpdateRequest request, MultipartFile profile);

  void delete(UUID userId);

}
