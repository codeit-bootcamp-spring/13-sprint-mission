package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ProfileImageCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserResponse create(UserCreateRequest userRequest, Optional<ProfileImageCreateRequest> profileImageRequest);

    UserResponse find(UUID id);

    List<UserResponse> findAll();

    UserResponse update(UUID id, UserUpdateRequest updateUserRequest, Optional<ProfileImageCreateRequest> profileImageRequest);

    void delete(UUID id);

}
