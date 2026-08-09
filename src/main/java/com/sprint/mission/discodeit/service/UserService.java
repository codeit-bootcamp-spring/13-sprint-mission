package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.util.*;

public interface UserService {

    UserDto create(CreateUserCommand command, CreateBinaryContentCommand profileImage);

    UserDto findByUserId(UUID userId);

    List<UserDto> findAll();

    UserDto update(UUID id, UpdateUserCommand command, CreateBinaryContentCommand profileImage);

    void delete(UUID userId);

}
