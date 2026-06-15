package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.CreateUserStatusInput;
import com.sprint.mission.discodeit.dto.input.UpdateUserStatusInput;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {
    void create(CreateUserStatusInput cusi);
    UserStatus find(UUID id);
    List<UserStatus> findAll();
    void update(UpdateUserStatusInput uusi);
    void updateByUserID(UpdateUserStatusInput uusi);
    void delete(UUID id);
}
