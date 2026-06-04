package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Repository
public class FileUserRepository implements UserRepository {

    private final List<User> users = new ArrayList<>();
    private final Path userPath;

    public FileUserRepository(Path userPath) {
        this.userPath = userPath;
        loadFromFile();
    }


    @Override
    public void create(User user) {
        users.add(user);
        saveToFile();
    }

    public boolean exists(UUID id) {
        return users.stream().anyMatch(user -> user.getId().equals(id));
    }

    @Override
    public User read(UUID id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }

    @Override
    public void update(UUID id, User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(id)) {
                users.set(i, user);
                saveToFile();
                return;
            }
        }
    }

    @Override
    public List<User> readAll() {
        return new ArrayList<>(users);
    }

    @Override
    public void delete(UUID id) {
        users.removeIf(user -> user.getId().equals(id));
        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = userPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(userPath)))) {

                oos.writeObject(users);
            }

        } catch (IOException e) {
            throw new RuntimeException("유저 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    private void loadFromFile() {
        if (!Files.exists(userPath)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(userPath)))) {

            List<User> loadedUsers = (List<User>) ois.readObject();

            users.clear();
            users.addAll(loadedUsers);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("유저 파일 불러오기 중 오류가 발생했습니다.", e);
        }
    }
}
