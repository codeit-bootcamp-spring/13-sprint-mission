package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFUser implements UserService {

    private final Map<UUID, User> data;

    public JCFUser() {

        this.data = new HashMap<>();
    }

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

        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {

        data.remove(id);
    }
}
