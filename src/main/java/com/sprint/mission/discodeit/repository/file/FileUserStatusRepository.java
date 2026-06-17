package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileUserStatusRepository implements UserStatusRepository {

    private Map<UUID, UserStatus> database;
    private final Path filePath;

    public FileUserStatusRepository(String fileDirectory) {
        this.filePath = Path.of(fileDirectory, "userStatus.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("데이터 디렉토리 생성 실패", e);
        }

        this.database = load();
    }



    @Override
    public User save(UserStatus userStatus) {
        database.put(userStatus.getId(), userStatus);
        saveToFile();

        return userStatus.getUser();
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        for (UserStatus userStatus : database.values()) {
            if (userStatus.getUser() != null && userStatus.getUser().getId().equals(userId)) {
                return Optional.of(userStatus);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public User delete(UUID userId) {
        Optional<UserStatus> targetOptional = findByUserId(userId);

        if (targetOptional.isPresent()) {
            UserStatus target = targetOptional.get();
            User user = target.getUser();

            database.remove(target.getId());
            saveToFile();
            return user;
        }
        return null;
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(database);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, UserStatus> load() {

        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (Map<UUID, UserStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일 로드 실패", e);
        }
    }

}
