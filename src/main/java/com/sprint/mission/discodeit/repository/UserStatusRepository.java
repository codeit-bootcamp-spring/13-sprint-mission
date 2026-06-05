package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public interface UserStatusRepository {

    User save(UserStatus user);
    UserStatus findByUserId(UUID userId);
    User delete(UUID userId);


}
