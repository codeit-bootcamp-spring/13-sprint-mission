package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.util.*;

public interface UserStatusService {

    UserStatusDto create(CreateUserStatusCommand command);

    UserStatusDto findByUserId(UUID userId);

    void delete(UUID id);

    UserStatusDto update(UUID id, UpdateUserStatusCommand command);

    UserStatusDto updateByUserId(UUID userId, UpdateUserStatusCommand command);
}