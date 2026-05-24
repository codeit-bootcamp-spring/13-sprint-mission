package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileUserRepository extends FileRepositoryRoot<User> implements UserRepository {

    //ctor
    public FileUserRepository() {
        super(Path.of("data/users.ser"));
    }

    //interface
    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public boolean existsUser(String email) {
        for (User user : storage) {
            if (user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createUser(User user) {
        storage.add(user);
        saveToBinary();
    }

    @Override
    public Optional<User> findUser(User user) {
        if (storage.contains(user)){
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return storage;
    }

    @Override
    public void deleteUser(User user) {
        storage.remove(user);
        saveToBinary();
    }
}
