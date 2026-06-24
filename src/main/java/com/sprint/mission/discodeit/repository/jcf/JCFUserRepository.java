package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.*;

public class JCFUserRepository implements UserRepository {

    private final List<User> data;

    public JCFUserRepository() {
        this.data = new ArrayList<>();
    }

    @Override
    public void save(User user) {
        data.add(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        for (User user : data) {
            if (user.getId().equals(id)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        if (!data.isEmpty()) {
            return data;
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(UUID id) {
        for (User user : data) {
            if (user.getId().equals(id)) {
                data.remove(user);
                return;
            }
        }
        throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }
}
