package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface UserStatusService {

    UserStatus create(CreateUserStatusRequest request);

    UserStatus find(UUID id);

    List<UserStatus> findAll();

    void delete(UUID id);

    UserStatus update(UUID id, UpdateUserStatusRequest request);

    void updateByUserId(UUID userId, UpdateUserStatusRequest request);

}
