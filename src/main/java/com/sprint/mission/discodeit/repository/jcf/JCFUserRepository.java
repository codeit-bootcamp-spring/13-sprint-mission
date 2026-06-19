package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", matchIfMissing = true, havingValue = "jcf")
public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> data;

    public JCFUserRepository() {
        this.data = new HashMap<>();
    }


    @Override
    public void create(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public boolean exists(UUID id) {
        return data.containsKey(id);
    }

    @Override
    public User find(UUID id) {
        return data.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(UUID id, User user) {
        data.put(id, user);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }

    @Override
    public User findByEmail(String email) {
        return data.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User findByUserName(String userName) {
        return data.values().stream()
                .filter(user -> user.getUserName().equals(userName))
                .findFirst()
                .orElse(null);
    }
}
