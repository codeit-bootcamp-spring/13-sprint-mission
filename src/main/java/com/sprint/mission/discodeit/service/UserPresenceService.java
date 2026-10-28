package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.UserDto;
import java.util.UUID;

public interface UserPresenceService {

  UserDto update(UUID userId, boolean online);
}