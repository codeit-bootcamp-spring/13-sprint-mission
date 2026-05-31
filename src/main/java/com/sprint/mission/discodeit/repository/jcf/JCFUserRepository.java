package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFUserRepository implements UserRepository {
    private final Map<UUID, User>users;

    public JCFUserRepository() {
        this.users = new ConcurrentHashMap<UUID, User>();
    }


    @Override
    public void save(User user) {users.put(user.getId(), user);
    }

    @Override
    public User findById(UUID id) {
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void delete(UUID id) {
        users.remove(id);
    }
}
