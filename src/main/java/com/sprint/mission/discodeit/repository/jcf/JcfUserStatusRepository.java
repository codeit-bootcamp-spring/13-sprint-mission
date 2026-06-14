package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JcfUserStatusRepository implements UserStatusRepository {

    private final List<UserStatus> database = new ArrayList<>();

    @Override
    public void create(UserStatus userStatus) {
        database.add(userStatus);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return database.stream()
                .filter(us -> us.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(database);
    }

    @Override
    public void update(UserStatus userStatus) {
        findById(userStatus.getId()).ifPresent(us -> {
            database.remove(us);
            database.add(userStatus);
        });
    }

    @Override
    public void delete(UUID id) {
        database.removeIf(us -> us.getId().equals(id));
    }
}
