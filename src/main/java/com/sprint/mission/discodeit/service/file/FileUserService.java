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
    public User create(String username, String email, String password) {
        User user = new User(username, email, password);
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
    public User update(UUID userId, String newUsername, String newEmail, String newPassword) {
        User user = repository.findById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.updateProfile(newUsername);
        repository.save(user);

        return user;
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}
