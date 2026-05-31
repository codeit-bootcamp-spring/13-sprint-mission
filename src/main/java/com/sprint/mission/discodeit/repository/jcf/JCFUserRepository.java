package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User> data = new HashMap<>();

    @Override
    public void create(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public User read(UUID id) {
        return data.get(id);
    }

    @Override
    public List<User> readAll() {
        return data.values().stream().toList();
    }

    @Override
    public void update(User user) {
        if (data.containsKey(user.getId())) {
            data.put(user.getId(), user);
        }
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
