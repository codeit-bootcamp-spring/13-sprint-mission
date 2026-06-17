package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileReadStatusRepository implements ReadStatusRepository {

    private Map<UUID, ReadStatus> database;
    private final Path filePath;

    public FileReadStatusRepository(String fileDirectory) {
        this.filePath = Path.of(fileDirectory, "readStatus.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("데이터 디렉토리 생성 실패", e);
        }

        this.database = load();
    }


    @Override
    public ReadStatus save(ReadStatus readStatus) {
        database.put(readStatus.getId(), readStatus);
        saveToFile();
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        List<ReadStatus> result = new ArrayList<>();

        for (ReadStatus readStatus : database.values()) {
            if (readStatus.getUserId().equals(userId)) {
                result.add(readStatus);
            }
        }
        return result;
    }

    @Override
    public ReadStatus update(ReadStatus readStatus) {
        return save(readStatus);
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(database);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, ReadStatus> load() {

        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (Map<UUID, ReadStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일 로드 실패", e);
        }
    }
}
