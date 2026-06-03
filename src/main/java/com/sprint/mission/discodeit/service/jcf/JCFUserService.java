package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class JCFUserService implements UserService {

    private final UserRepository repository;

    public JCFUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public User update(UUID id, String username, String email) {

        User user = find(id);

        user.update(username, email);

        return repository.save(user);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}