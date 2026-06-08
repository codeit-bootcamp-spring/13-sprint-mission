package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;


public class JCFUserRepository implements UserRepository {
    private final HashMap<UUID, User> data = new HashMap<>();


    @Override
    public User save(User user) {
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public boolean existsById(UUID id) {
       return data.containsKey(id);
    }

    @Override
    public List<User> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public void deleteById(UUID id) {
        data.remove(id);
    }

}

