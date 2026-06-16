package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {

    private final Path filePath;

    //디렉토리 생성
    public FileUserStatusRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String fileDirectory){
        this.filePath = Path.of(fileDirectory).resolve("user_status.ser");
        if (!Files.exists(filePath.getParent())) {
            try {
                Files.createDirectories(filePath.getParent());
            }catch (IOException e){
                throw new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    //data 저장
    private void saveToFile(Map<UUID, UserStatus> data){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(data);
        }catch (IOException e){
            throw new RuntimeException("파일 저장 실패!");
        }
    }

    //파일 불러오기
    private Map<UUID, UserStatus> loadFromFile(){
        if (!Files.exists(filePath)){
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (Map<UUID, UserStatus>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 불러오기 실패!");
        }
    }


    @Override
    public void save(UserStatus userStatus) {
        Map<UUID, UserStatus> data = loadFromFile();
        data.put(userStatus.getId(), userStatus);
        saveToFile(data);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        Map<UUID, UserStatus> data = loadFromFile();
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        Map<UUID, UserStatus> data = loadFromFile();
        return data.values().stream()
                .filter(us -> us.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        Map<UUID, UserStatus> data = loadFromFile();
        return new ArrayList<>(data.values());
    }

    //삭제
    @Override
    public void deleteById(UUID id) {
        Map<UUID, UserStatus> data = loadFromFile();
        data.remove(id);
        saveToFile(data);
    }
}
