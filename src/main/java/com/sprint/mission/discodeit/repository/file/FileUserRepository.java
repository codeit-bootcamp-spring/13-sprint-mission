package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "file"
)
public class FileUserRepository extends FileRepositoryRoot<User> implements UserRepository {

    //ctor
    public FileUserRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        super(Path.of(fileDirectory).resolve("users.ser"));
    }

    //interface
    @Override
    public boolean existsUserById(UUID userId) {
        return storage.stream()
                .anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public boolean existsUserByName(String name) {
        return storage.stream()
                .anyMatch(user -> user.getName().equals(name));
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
    public Optional<User> findUserById(UUID userId) {
        return storage.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    @Override
    public Optional<User> findUserByNameAndPassword(String username, String password) {
        return storage.stream()
                .filter(user -> user.getName().equals(username) && user.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return storage;
    }

    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void deleteUser(UUID id) {
        storage.remove(
                storage.stream()
                        .filter(user -> user.getId().equals(id))
                        .findFirst()
                        .get()
        );

        saveToBinary();
    }
}
