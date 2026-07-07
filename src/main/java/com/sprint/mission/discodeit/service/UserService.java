package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import java.util.List;
import java.util.UUID;


public interface UserService {

    //(C)생성
    UserDto createUser(UserCreateRequest userRequest,
                       BinaryContentCreateRequest profileRequest);
    //(R)조회 단건
    UserDto findByUserId(UUID userId);
    //(R)조회 다수
    List<UserDto> findAllUser();
    //(U)수정
    UserDto updateUser(UUID userId, UserUpdateRequest updateRequest,
                            BinaryContentCreateRequest profileRequest);
    //(D)삭제
    void deleteUser(UUID userId);
}
