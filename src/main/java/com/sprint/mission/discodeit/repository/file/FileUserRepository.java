package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserRepository implements UserRepository {

    private final Path filePath;

    public FileUserRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}")String fileDirectory) {
        this.filePath = Path.of(fileDirectory).resolve("users.ser");
        if (!Files.exists(filePath.getParent())){
            try {
                Files.createDirectories(filePath.getParent());
            } catch (IOException e) {
                throw new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    private void saveToFile(Map<UUID, User> data){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패!");
        }
    }

    //파일에서 data 불러오기
    private Map<UUID, User> loadFromFile(){
        if (!Files.exists(filePath)){
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
            return (Map<UUID, User>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            System.out.println("에러 타입: " + e.getClass().getName());
            System.out.println("에러 메시지: " + e.getMessage());
            throw new RuntimeException("파일 불러오기 실패");
        }
    }

    @Override
    public void save(User user) {
        Map<UUID, User> data = loadFromFile();
        data.put(user.getUserId(), user);
        saveToFile(data);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        Map<UUID, User> data = loadFromFile();
        return Optional.ofNullable(data.get(userId));
    }

    @Override
    public List<User> findAll() {
        Map<UUID, User> data = loadFromFile();
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteById(UUID userId) {
        Map<UUID, User> data = loadFromFile();
        data.remove(userId);
        saveToFile(data);
    }
}
