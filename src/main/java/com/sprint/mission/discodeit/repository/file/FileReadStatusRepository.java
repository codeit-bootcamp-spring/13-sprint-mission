package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class FileReadStatusRepository implements ReadStatusRepository {

    private final File readStatusFile;
    private final Map<UUID, ReadStatus> readStatusRepo;
    private Map<UUID, ReadStatus> loadReadStatusRepo()  {
        if (!readStatusFile.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(readStatusFile))) {
            return (Map<UUID, ReadStatus>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();}
    }

    public FileReadStatusRepository() {
        this.readStatusFile = new File("data/repository-read-status.json");
        this.readStatusRepo = loadReadStatusRepo();
    }

    private void saveReadStatusRepo() {
        File parent = readStatusFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(readStatusFile))) {
            out.writeObject(readStatusRepo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void save(ReadStatus readStatus) {
        readStatusRepo.put(readStatus.getId(), readStatus);
        saveReadStatusRepo();

    }

    @Override
    public ReadStatus findById(UUID id) {
        return readStatusRepo.get(id);
    }

    @Override
    public Collection<ReadStatus> findAllByUserId(UUID userId) {
        List<ReadStatus> result = new ArrayList<>();

        for (ReadStatus readStatus : readStatusRepo.values()) {
            if (readStatus.getUserId().equals(userId)) {
                result.add(readStatus);
            }
        }
        return result;

    }

    @Override
    public void delete(UUID id) {
        readStatusRepo.remove(id);
        saveReadStatusRepo();

    }

    @Override
    public ReadStatus findByUserIdAndChannelId(UUID userId, UUID channelId) {
        for (ReadStatus readStatus : readStatusRepo.values()) {
            if (readStatus.getUserId().equals(userId)
                    && readStatus.getChannelId().equals(channelId)) {
                return readStatus;
            }
        }
        return null;
    }
}
