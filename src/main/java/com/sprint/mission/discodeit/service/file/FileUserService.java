package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileUserService implements Serializable, UserService {

    private static final long serialVersionUID = 1L;
    private final List<User> users = new ArrayList<>();
    private final Path userPath;

    public FileUserService(Path userPath) {
        this.userPath = userPath;
        loadFromFile();
    }

    @Override
    public void create(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("유저이름이 없습니다.");
        }
        users.add(user);
        saveToFile();
    }

    @Override
    public User read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
    }

    @Override
    public List<User> readAll() {
        return new ArrayList<>(users);
    }

    @Override
    public void update(UUID id, User user) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수 없습니다.");
        }

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("유저가 없습니다.");
        }

        for (int i = 0; i < users.size(); i++) {

            if (users.get(i).getId().equals(id)) {

                users.set(i, user);
                saveToFile();
                return;
            }
        }

        throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        boolean removed = users.removeIf(user -> user.getId().equals(id));

        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = userPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos =
                         new ObjectOutputStream(
                                 new BufferedOutputStream(
                                         Files.newOutputStream(userPath)))) {

                oos.writeObject(new ArrayList<>(users));
            }

        } catch (IOException e) {
            throw new RuntimeException("유저 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(userPath)) {
            return;
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new BufferedInputStream(
                                     Files.newInputStream(userPath)))) {

            users.addAll((List<User>) ois.readObject());

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("유저 파일 불러오기 중 오류가 발생했습니다.", e);
        }
    }

}

