package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import java.util.List;
import java.util.UUID;


public interface UserService {

    //(C)생성
    UserDto createUser(UserCreateCommand command,
                       BinaryContentCreateCommand profileRequest);
    //(R)조회 단건
    UserDto findByUserId(UUID userId);
    //(R)조회 다수
    List<UserDto> findAllUser();
    //(U)수정
    UserDto updateUser(UUID userId, UserUpdateCommand command,
                       BinaryContentCreateCommand profileRequest);
    //(D)삭제
    void deleteUser(UUID userId);
}
