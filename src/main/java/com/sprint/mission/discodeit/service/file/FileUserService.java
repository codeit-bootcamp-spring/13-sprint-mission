package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileUserService implements UserService {

    public static final Path FILE_PATH = Path.of("data/users.ser");

    private final Map<UUID, User> userMap;

    public FileUserService() {
        this.userMap = loadFromFile();
    }


    @Override
    public User create(String username, String email, String password, UserStatus userStatus) {
        User user = new User(username, email, password, userStatus);
        userMap.put(user.getId(), user);
        saveToFile();  // 변경됐으니 저장
        return user;
    }

    @Override
    public User read(UUID id) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        return user;
    }

    // JCF랑 확인해보기
    @Override
    public List<User> readAll() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User update(UUID id, String username, String password, UserStatus userStatus) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updateUsername(username);
        user.updatePassword(password);
        user.updateUserStatus(userStatus);
        saveToFile();  // 변경됐으니 저장
        return user;
    }

    public User updateUsername(UUID id, String username) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updateUsername(username);
        saveToFile();
        return user;
    }

    public User updatePassword(UUID id, String password) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updatePassword(password);
        saveToFile();
        return user;
    }

    public User updateUserStatus(UUID id, UserStatus userStatus) {
        User user = userMap.get(id);
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        user.updateUserStatus(userStatus);
        saveToFile();
        return user;
    }
    @Override
    public void delete(UUID id) {
        userMap.remove(id);
        saveToFile();  // 변경됐으니 저장
    }


    private void saveToFile() {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(FILE_PATH)))) {
                oos.writeObject(new LinkedHashMap<>(userMap));
            }
            System.out.println("유저 저장을 완료하였습니다." + FILE_PATH.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("유저 저장에 실패했습니다.", e);
        }
    }

    private Map<UUID, User> loadFromFile() {
        if (!Files.exists(FILE_PATH)) {
            return new LinkedHashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(FILE_PATH)))) {
            Map<UUID, User> map = (Map<UUID, User>) ois.readObject();
            System.out.println("유저 불러오기를 완료하였습니다." + FILE_PATH.toAbsolutePath());
            return map;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("유저 불러오기에 실패했습니다.", e);
        }
    }
}
