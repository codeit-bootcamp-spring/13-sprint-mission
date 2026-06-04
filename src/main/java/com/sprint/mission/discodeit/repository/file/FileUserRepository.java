package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.*;

public class FileUserRepository implements UserRepository {
    private final File file;
    private final Map<UUID, User> users;
    private Map<UUID, User> loadUserRepo() {
        if (!file.exists()) {
            return new HashMap<UUID, User>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, User>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<UUID, User>();
        }
    }

    private void saveUserRepo() {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(users);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileUserRepository() {
        this.file = new File("data/repository-user.ser");
        this.users = loadUserRepo();
    }


    @Override
    public void save(User user) {
        users.put(user.getId(),user);
        saveUserRepo();
    }

    @Override
    public User findById(UUID id) {
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void delete(UUID id) {
        users.remove(id);
        saveUserRepo();
    }
}
