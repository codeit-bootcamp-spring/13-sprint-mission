package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    UserResponse find(UUID userId);
    List<UserResponse> findAll();
    UserResponse update(UUID userId, UserUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다
    void delete(UUID userId);
}
