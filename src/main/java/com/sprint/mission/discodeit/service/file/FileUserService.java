package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUserService implements UserService {

    // users 디렉토리 경로
    private final Path directory;

    public FileUserService() {
        this.directory = Paths.get(
                System.getProperty("user.dir"),
                "data",
                "users"
        );
        init(directory);
    }

    public static void init(Path directory) {
        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void createUser(User user) {
        Path filePath =
                directory.resolve(user.getId() + ".ser");

        save(filePath, user);
    }

    // 단일 조회
    @Override
    public User findUser(UUID id) {
        Path filePath =
                directory.resolve(id + ".ser");
        if (!Files.exists(filePath)) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 유저 전체 조회
    @Override
    public List<User> findAllUsers() {
        if (Files.exists(directory)) {
            try {
                List<User> list = Files.list(directory)
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return (User) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateUser(UUID id, String name, String email, String password) {
        User user = findUser(id);
        if (user == null) {
            return;
        }

        user.update(name, email, password);

        Path filePath = directory.resolve(id + ".ser");

        save(filePath, user);
    }

    @Override
    public void deleteUser(UUID id) {
        Path filePath = directory.resolve(id + ".ser");

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 객체 저장
    private <T> void save(Path filePath, T data) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
