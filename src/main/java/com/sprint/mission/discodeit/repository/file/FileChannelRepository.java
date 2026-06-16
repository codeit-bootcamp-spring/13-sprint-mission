package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {

    private final Path filePath;

    public FileChannelRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String fileDirectory
    ){
        this.filePath =  Path.of(fileDirectory).resolve("channel.ser");
        if (!Files.exists(filePath.getParent())){
            try {
                Files.createDirectories(filePath.getParent());
            }catch (IOException e){
                throw new RuntimeException("디렉토리 생성 실패");
            }
        }
    }

    private void saveToFile(Map<UUID, Channel > data){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))){
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패!");
        }
    }

    private Map<UUID, Channel> loadFromFile(){
        if (!Files.exists(filePath)){
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))){
            return (Map<UUID, Channel>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 불러오기 실패");
        }
    }




    @Override
    public void save(Channel channel) {
        Map<UUID, Channel> data = loadFromFile();
        data.put(channel.getChannelId(), channel);
        saveToFile(data);
    }

    @Override
    public Optional<Channel> findById(UUID channelId) {
        Map<UUID, Channel> data = loadFromFile();
        return Optional.ofNullable(data.get(channelId));
    }

    @Override
    public List<Channel> findAll() {
        Map<UUID, Channel> data = loadFromFile();
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteById(UUID channelId) {
        Map<UUID, Channel> data = loadFromFile();
        data.remove(channelId);
        saveToFile(data);
    }
}
