package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {

    private final List<ReadStatus> readStatuses = new ArrayList<>();
    private final Path readStatusPath;

    public FileReadStatusRepository(Path readStatusPath) {
        this.readStatusPath = readStatusPath;
        loadFromFile();
    }

    public FileReadStatusRepository() {
        this(Paths.get("data/readStatuses.ser"));
    }

    @Override
    public void create(ReadStatus readStatus) {
        readStatuses.add(readStatus);
        saveToFile();
    }

    @Override
    public ReadStatus find(UUID id) {
        return readStatuses.stream()
                .filter(readStatus -> readStatus.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(readStatuses);
    }

    @Override
    public void delete(UUID id) {
        readStatuses.removeIf(readStatus -> readStatus.getId().equals(id));
        saveToFile();
    }

    @Override
    public List<ReadStatus> findByChannelId(UUID channelId) {
        return readStatuses.stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return readStatuses.stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }

    @Override
    public ReadStatus findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return readStatuses.stream()
                .filter(readStatus ->
                        readStatus.getUserId().equals(userId)
                                && readStatus.getChannelId().equals(channelId)
                )
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(ReadStatus readStatus) {
        readStatuses.removeIf(status -> status.getId().equals(readStatus.getId()));
        readStatuses.add(readStatus);
        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = readStatusPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try(ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(readStatusPath)))) {
                oos.writeObject(readStatuses);
            }
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(readStatusPath)) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(readStatusPath)))) {
            List<ReadStatus> loadedReadStatuses = (List<ReadStatus>) ois.readObject();
            readStatuses.clear();
            readStatuses.addAll(loadedReadStatuses);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일을 불러오는 중 오류가 발생했습니다.", e);
        }
    }
}
