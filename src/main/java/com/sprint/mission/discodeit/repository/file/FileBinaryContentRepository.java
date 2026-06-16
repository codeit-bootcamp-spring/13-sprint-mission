package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> database;
    private final Path filePath;

    public FileBinaryContentRepository(String fileDirectory) {
        this.filePath = Path.of(fileDirectory, "binary.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("데이터 디렉토리 생성 실패", e);
        }

        this.database = load();
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        database.put(binaryContent.getId(), binaryContent);
        saveToFile();
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
        saveToFile();
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
    private Map<UUID, BinaryContent> load() {

        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(filePath))) {

            return (Map<UUID, BinaryContent>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일 로드 실패", e);
        }
    }
}
