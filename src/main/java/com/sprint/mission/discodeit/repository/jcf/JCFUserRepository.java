package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.*;

public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> users = new HashMap<>();

    @Override
    public User save(User user) {

        users.put(user.getId(), user);

        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {

        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {

        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(UUID id) {

        users.remove(id);
    }
}