package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
public class FileReadStatusRepository implements ReadStatusRepository {

    private final Path filePath;

    //디렉토리 생성
    public FileReadStatusRepository(){
        this.filePath = Path.of("data/read_status.ser");
        if (!Files.exists(filePath.getParent())) {
            try {
                Files.createDirectories(filePath.getParent());
            }catch (IOException e){
                throw new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    //data 저장
    private void saveToFile(Map<UUID, ReadStatus> data){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(data);
        }catch (IOException e){
            throw new RuntimeException("파일 저장 실패!");

        }
    }


    //파일 불러오기
    private Map<UUID, ReadStatus> loadFromFile(){
        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (Map<UUID, ReadStatus>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 불러오기 실패");
        }
    }

    @Override
    public void save(ReadStatus readStatus) {
        Map<UUID, ReadStatus> data = loadFromFile();
        data.put(readStatus.getId(), readStatus);
        saveToFile(data);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        Map<UUID, ReadStatus> data = loadFromFile();
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        Map<UUID, ReadStatus> data = loadFromFile();
        return data.values().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        Map<UUID, ReadStatus> data = loadFromFile();
        return data.values().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        Map<UUID, ReadStatus> data = loadFromFile();
        data.remove(id);
        saveToFile(data);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        Map<UUID, ReadStatus> data = loadFromFile();
        data.values().removeIf(rs -> rs.getChannelId().equals(channelId));
        saveToFile(data);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        Map<UUID, ReadStatus> data = loadFromFile();
        data.values().removeIf(rs -> rs.getUserId().equals(userId));
        saveToFile(data);
    }
}
