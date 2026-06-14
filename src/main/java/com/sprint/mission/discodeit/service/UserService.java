package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserResponse create(UserRequest dto); // 유저 생성하는 기능 (추상 메서드 선언)
    Optional<UserResponse> findById(UUID id);
    List<UserResponse> findAll();
    UserResponse update(UUID id, UserRequest dto);
    void delete(UUID id);
}



