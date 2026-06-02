package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
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
    public boolean existsUserByEmail(String email) {
        return storage.stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public void createUser(User user) {
        storage.add(user);
        saveToBinary();
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return storage.stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
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
