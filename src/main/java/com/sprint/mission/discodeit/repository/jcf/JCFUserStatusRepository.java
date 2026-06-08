package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.*;

public class JCFUserStatusRepository implements UserStatusRepository {

    private final Map<UUID, UserStatus> database = new HashMap<>();

    @Override
    public User save(UserStatus userStatus) {
        database.put(userStatus.getId(), userStatus);
        return userStatus.getUser();
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return database.values().stream()
                .filter(us -> us.getUser() != null && us.getUser().getId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public User delete(UUID userId) {
        Optional<UserStatus> targetOptional = findByUserId(userId);

        if (targetOptional.isPresent()) {
            UserStatus target = targetOptional.get();
            User user = target.getUser();

            database.remove(target.getId());
            return user;
        }
        return null;
    }
}
