package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.*;

public class JCFUserRepository implements UserRepository {
    // 데이터를 메모리에 저장
    private final Map<String, User> data = new HashMap<>();

    @Override
    public void save(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(User user) {
        data.put(user.getId(), user);
    }

    @Override
    public void delete(String id) {
        data.remove(id);
    }
}