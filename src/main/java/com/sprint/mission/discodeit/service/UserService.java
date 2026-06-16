package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.CreateProfileImageRequest;
import com.sprint.mission.discodeit.dto.request.CreateUserRequest;
import com.sprint.mission.discodeit.dto.request.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User create(CreateUserRequest userRequest, Optional<CreateProfileImageRequest> profileImageRequest);

    UserResponse find(UUID id);

    List<User> findAll();

    User update(UpdateUserRequest updateUserRequest, Optional<CreateProfileImageRequest> profileImageRequest);

    void delete(UUID id);

}
