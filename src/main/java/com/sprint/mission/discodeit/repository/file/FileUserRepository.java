package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.*;

public class FileUserRepository implements UserRepository {
    private final File file;
    private Map<UUID, User> data;
    public FileUserRepository(String path) {
        this.file = new File(path);
        this.data = load();
    }

    @Override public User save(User user) {
        data.put(user.getId(), user);
        saveToFile();
        return user;
    }

    @Override public Optional<User> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override public void delete(UUID id) {
        data.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(
                             new FileOutputStream(file))) {
            oos.writeObject(data);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<UUID, User> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            return (Map<UUID, User>) ois.readObject();
        }

        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}