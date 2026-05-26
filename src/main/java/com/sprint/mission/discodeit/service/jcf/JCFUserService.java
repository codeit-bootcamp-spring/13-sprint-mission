package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFUserService implements UserService {

    private final UserRepository repository;

    public JCFUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(User user) {
        repository.create(user);
    }

    @Override
    public User read(UUID id) {
        return repository.read(id);
    }

    @Override
    public List<User> readAll() {
        return repository.readAll();
    }

    @Override
    public void update(UUID id, User user) {
        repository.update(id, user);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}

