package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {
    @Override
    public User save(UserStatus user) {
        return null;
    }

    @Override
    public UserStatus findByUserId(UUID userId) {
        return null;
    }

    @Override
    public User delete(UUID userId) {
        return null;
    }
}
