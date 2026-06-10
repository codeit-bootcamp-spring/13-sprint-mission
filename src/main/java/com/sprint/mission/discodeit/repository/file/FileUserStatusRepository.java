package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Repository
public class FileUserStatusRepository implements UserStatusRepository {

    private final List<UserStatus> userStatuses = new ArrayList<>();
    private final Path userStatusPath;

    public FileUserStatusRepository(Path userStatusPath) {
        this.userStatusPath = userStatusPath;
        loadFromFile();
    }

    public FileUserStatusRepository() {
        this(Paths.get("data/userStatuses.ser"));
    }

    @Override
    public UserStatus findByUserId(UUID id) {
        return userStatuses.stream()
                .filter(userStatus -> userStatus.getUserId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void create(UserStatus userStatus) {
        userStatuses.add(userStatus);
        saveToFile();
    }

    @Override
    public UserStatus find(UUID id) {
        return userStatuses.stream()
                .filter(userStatus -> userStatus.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<UserStatus> findAll() {
        return List.copyOf(userStatuses);
    }

    @Override
    public void delete(UUID id) {
        userStatuses.removeIf(userStatus ->
                Objects.equals(userStatus.getId(), id)
        );
        saveToFile();
    }

    @Override
    public void update(UserStatus userStatus) {
        int index = userStatuses.indexOf(find(userStatus.getId()));

        if (index != -1) {
            userStatuses.set(index, userStatus);
            saveToFile();
        }
    }

    private void saveToFile() {
        try {
            Path parent = userStatusPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(userStatusPath)))) {
                oos.writeObject(userStatuses);
            }

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(userStatusPath)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(userStatusPath)))) {

            List<UserStatus> loadedUserStatuses = (List<UserStatus>) ois.readObject();
            userStatuses.clear();
            userStatuses.addAll(loadedUserStatuses);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일을 불러오는 중 오류가 발생했습니다.", e);
        }
    }
}
