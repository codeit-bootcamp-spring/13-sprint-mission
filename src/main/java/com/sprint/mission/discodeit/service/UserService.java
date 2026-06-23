package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.input.BinaryContentCreate;
import com.sprint.mission.discodeit.dto.input.UserCreateRequest;
import com.sprint.mission.discodeit.dto.input.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.output.UserDto;
import com.sprint.mission.discodeit.entity.User;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User create(UserCreateRequest upf, Optional<BinaryContentCreate> bcc);
    List<UserDto> getUserList();
    User update(UUID id, UserUpdateRequest uui, Optional<BinaryContentCreate> bcc);
    void delete(UUID id);
}
