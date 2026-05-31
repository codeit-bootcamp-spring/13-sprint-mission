package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class FileUserRepository implements UserRepository {
    private final Path path;

    public FileUserRepository(String path) {
        this.path = Paths.get(path);
    }

    private void saveFile(List<User> users) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(users));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(User user) {
        List<User> users = loadFile();
        users.add(user);
        saveFile(users);
    }

    @Override
    public User read(UUID id) {
        List<User> users = loadFile();
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> readAll() {
        return loadFile();
    }

    public void update(User user) {
        List<User> users = loadFile();

        for (User u : users) {
            if (u.getId().equals(user.getId())) {
                u.update(user.getUserName(), user.getPw(), user.getEmail());
                break;
            }
        }
        saveFile(users);
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadFile();

        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }
        saveFile(users);
    }
}
