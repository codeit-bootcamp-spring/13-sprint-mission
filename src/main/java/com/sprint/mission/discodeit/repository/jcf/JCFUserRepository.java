package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(
        prefix = "discodeit.repository",
        name = "type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> data;

    public JCFUserRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public void save(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public User findById(UUID id) {
        return data.get(id);
    }

    @Override
    public User findByUserName(String userName) {
        return data.values().stream()
                .filter(u->u.getUserName().equals(userName))
                .findFirst().orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return data.values().stream()
                .filter(u->u.getEmail().equals(email))
                .findFirst().orElse(null);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
