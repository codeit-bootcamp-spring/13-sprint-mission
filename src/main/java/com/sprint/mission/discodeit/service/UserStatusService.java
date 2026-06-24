package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface UserStatusService {

    UserStatusResponse create(CreateUserStatusRequest request);

    UserStatusResponse find(UUID id);

    List<UserStatusResponse> findAll();

    void delete(UUID id);

    UserStatusResponse update(UUID id, UpdateUserStatusRequest request);

    UserStatusResponse updateByUserId(UUID userId, UpdateUserStatusRequest request);

}
