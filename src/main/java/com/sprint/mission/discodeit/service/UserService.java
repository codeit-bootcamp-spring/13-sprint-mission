package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.request.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    UserDto create(UserCreateRequest upf, Optional<BinaryContentCreate> bcc);
    List<UserDto> getUserList();
    UserDto update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> bcc);
    void delete(UUID id);
}
