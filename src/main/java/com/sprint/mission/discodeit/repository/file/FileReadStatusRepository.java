package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileReadStatusRepository implements ReadStatusRepository {

    private final String filePath = "read_status.dat";

    @SuppressWarnings("unchecked")
    private List<ReadStatus> loadAll() {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ReadStatus>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveAll(List<ReadStatus> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(ReadStatus readStatus) {
        List<ReadStatus> list = loadAll();
        list.add(readStatus);
        saveAll(list);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return loadAll().stream().filter(rs -> rs.getId().equals(id)).findFirst();
    }

    @Override
    public List<ReadStatus> findAll() {
        return loadAll();
    }

    @Override
    public void update(ReadStatus readStatus) {
        List<ReadStatus> list = loadAll();
        list.removeIf(rs -> rs.getId().equals(readStatus.getId()));
        list.add(readStatus);
        saveAll(list);
    }

    @Override
    public void delete(UUID id) {
        List<ReadStatus> list = loadAll();
        list.removeIf(rs -> rs.getId().equals(id));
        saveAll(list);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        List<ReadStatus> list = loadAll();
        list.removeIf(rs -> rs.getChannelId().equals(channelId));
        saveAll(list);
    }
}
