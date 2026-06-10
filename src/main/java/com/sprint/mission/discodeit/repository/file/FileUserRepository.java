package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User; // 💡 1. 대상 엔티티 변경
import com.sprint.mission.discodeit.repository.UserRepository; // 💡 2. 인터페이스 변경
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileUserRepository implements UserRepository {

    private final Map<UUID, User> database; // 💡 1. 타입 변경
    private final Path filePath;

    public FileUserRepository(String fileDirectory) {
        this.filePath = Path.of(fileDirectory, "user.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("데이터 디렉토리 생성 실패", e);
        }

        this.database = load();
    }

    @Override
    public User save(User user) { //
        database.put(user.getId(), user);
        saveToFile();
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
        saveToFile();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return database.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return database.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(filePath))) {

            oos.writeObject(database);

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, User> load() {

        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(filePath))) {

            return (Map<UUID, User>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일 로드 실패", e);
        }
    }
}
