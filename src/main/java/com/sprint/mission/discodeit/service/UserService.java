package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import java.util.List;
import java.util.UUID;


public interface UserService {

    //(C)생성
    UserResponse createUser(UserCreateRequest userRequest,
                            BinaryContentCreateRequest profileRequest);
    //(R)조회 단건
    UserResponse findByUser(UUID userId);
    //(R)조회 다수
    List<UserResponse> findAllUser();
    //(U)수정
    UserResponse updateUser(UUID userId, UserUpdateRequest updateRequest,
                            BinaryContentCreateRequest profileRequest);
    //(D)삭제
    void deleteUser(UUID userId);
}
