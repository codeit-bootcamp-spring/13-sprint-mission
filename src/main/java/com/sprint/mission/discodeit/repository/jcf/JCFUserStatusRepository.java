package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserStatusRepository implements UserStatusRepository {

    private final List<UserStatus> data = new ArrayList<>();

    @Override
    public UserStatus save(UserStatus userStatus) {
        data.removeIf(s -> s.getId().equals(userStatus.getId()));
        data.add(userStatus);
        return userStatus;
    }

    @Override
    public UserStatus findById(UUID id) {
        return data.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<UserStatus> findAll() {
        return data;
    }

    @Override
    public void delete(UUID id) {
        data.removeIf(s -> s.getId().equals(id));
    }

    @Override
    public UserStatus findByUserId(UUID userId) {
        return data.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

}
