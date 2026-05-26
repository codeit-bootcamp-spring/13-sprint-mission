package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserRepository implements UserRepository {

    private List<User> storage;
    private final Path filePath;

    public FileUserRepository() {

        this.filePath = Path.of("data/users.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch(IOException e){
            throw new RuntimeException(e);
        }

        storage = load();
    }

    @Override
    public User save(User user) {

        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(user.getId())) {
                storage.remove(i);
                break;
            }
        }

        storage.add(user);
        saveToFile();
        return user;

    }

    @Override
    public User findById(UUID id) {
        for (User user : storage) {
            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public void delete(UUID id) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(id)) {
                storage.remove(i);
                break;
            }
        }

        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(storage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> load() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
