package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileUserService implements UserService {
    private final Path path;

    public FileUserService(String path) {
        this.path = Paths.get(path);
    }

    private void saveUserFile(List<User> user) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(user));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<User> loadUserFile() {
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
    public User create(String userName, String pw, String email) {
        User user = new User(userName, pw, email);
        List<User> users = loadUserFile();

        users.add(user);
        saveUserFile(users);

        System.out.println(userName + " 계정이 생성되었습니다!");
        return user;
    }

    @Override
    public User read(UUID id) {
        List<User> users = loadUserFile();

        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        System.out.println("계정이 존재하지 않습니다.");
        return null;
    }

    @Override
    public List<User> readAll() {
        return loadUserFile();
    }

    @Override
    public void update(UUID id, String userName, String pw, String email) {
        List<User> users = loadUserFile();

        for (User user : users) {
            if (user.getId().equals(id)) {
                user.update(userName, pw, email);
                break;
            }
        }
        saveUserFile(users);
    }

    @Override
    public void delete(UUID id) {
        List<User> users = loadUserFile();

        for (User user : users) {
            if(user.getId().equals(id)) {
                users.remove(user);
                break;
            }
        }

        saveUserFile(users);
    }
}
