package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class FileUserService implements UserService {

    private final UserRepository repository;

    public FileUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
        return repository.save(user);
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public void update(UUID id, String newUsername) {
        User user = repository.findById(id);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.updateProfile(newUsername);
        repository.save(user);

    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}
