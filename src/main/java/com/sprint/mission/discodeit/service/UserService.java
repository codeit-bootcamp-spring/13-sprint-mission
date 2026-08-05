package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserResponse create(UserRequest dto, BinaryContentRequest profile);
    Optional<UserResponse> findById(UUID id);
    List<UserResponse> findAll();
    UserResponse update(UUID id, UserRequest dto, BinaryContentRequest profileDto);
    void delete(UUID id);
}



