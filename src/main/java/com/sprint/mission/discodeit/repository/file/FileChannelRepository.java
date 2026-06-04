package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {
    private final Path filePath = Paths.get("channels.ser");

    private List<Channel> loadFromFile(){
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))){
            return (List<Channel>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 로드 실패", e);
        }
    }

    private void saveToFile(List<Channel> channels){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))){
            oos.writeObject(channels);
        }catch (IOException e){
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public void save(Channel channel) {
        List<Channel> channels = loadFromFile();
        channels.add(channel);
        saveToFile(channels);
    }

    @Override
    public Channel findById(UUID id) {
        List<Channel>channels = loadFromFile();
        for (Channel channel : channels){
            if (channel.getId().equals(id)){
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> findAll() {
        return loadFromFile();
    }

    @Override
    public void delete(UUID id) {
        List<Channel>channels = loadFromFile();
        channels.remove(findById(id));
        saveToFile(channels);

    }
}
