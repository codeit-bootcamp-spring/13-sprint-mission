package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {
    // 유저 생성 기능
    UserResponse create(UserCreateRequest request);
    // 유저 하나 읽기 기능
    UserResponse read(UUID id);
    // 유저 전부 읽기 기능
    List<UserResponse> readAll();
    // 유저 수정 기능
    UserResponse update(UserUpdateRequest request);
    // 유저 삭제 기능
    void delete(UUID id);
    // 유저 권한 수정 기능
    UserResponse updateRole(UserRoleUpdateRequest request);

}
