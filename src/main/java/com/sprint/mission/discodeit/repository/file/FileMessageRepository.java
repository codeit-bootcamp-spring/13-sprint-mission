package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {

    private final Path filePath;

    public FileMessageRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String fileDirectory
    ) {
        this.filePath = Path.of(fileDirectory).resolve("message.ser");
        if (!Files.exists(filePath.getParent())) {
            try {
                Files.createDirectories(filePath.getParent());
            } catch (IOException e){
                throw  new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    // 파일에 data 저장하기
    private void saveToFile(Map<UUID, Message> data){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패!");
        }
    }

    //파일에서 data 불러오기
    private Map<UUID, Message> loadFromFile(){
        if (!Files.exists(filePath)){
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
            return (Map<UUID, Message>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            System.out.println("에러 타입: " + e.getClass().getName());
            System.out.println("에러 메시지: " + e.getMessage());
            throw new RuntimeException("파일 불러오기 실패");
        }
    }

    @Override
    public void save(Message message) {
        Map<UUID, Message> data = loadFromFile();
        data.put(message.getMessageId(), message);
        saveToFile(data);
    }

    @Override
    public Optional<Message> findById(UUID messageId) {
        Map<UUID, Message> data = loadFromFile();
        return Optional.ofNullable(data.get(messageId));
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        Map<UUID, Message> data = loadFromFile();
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        Map<UUID, Message> data = loadFromFile();
        data.remove(id);
        saveToFile(data);
    }
}
