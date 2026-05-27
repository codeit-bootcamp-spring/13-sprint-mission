package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {

    private final UserService userService;

    public FileChannelService(UserService userService) {
        this.userService = userService;
    }

    private final Path filePath = Paths.get("channel.ser");

    private List<Channel> loadFromFile() {
        if (!Files.exists(filePath)){
            return new ArrayList<>();
        }try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toString()))) {
            return (List<Channel>) ois.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RuntimeException("파일 로드 실패", e);
        }
    }
    private void saveToFile(List<Channel> channels) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(channels);
        }catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }



    @Override
    public void create(Channel channel) {
        List<Channel> channels = loadFromFile();
        channels.add(channel);
        saveToFile(channels);
    }

    @Override
    public Channel read(UUID id) {
        List<Channel> channels = loadFromFile();
        for(Channel channel : channels ){
            if (channel.getId().equals(id)){
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> readAll() {
        return loadFromFile();
    }

    @Override
    public void update(UUID id, String name, String description) {
        List<Channel> channels = loadFromFile();
        Channel foundCh = read(id);
        if (foundCh != null){
            foundCh.update(name, description);
            saveToFile(channels);
        }

    }

    @Override
    public void delete(UUID id) {
        List<Channel> channels = loadFromFile();
        channels.remove(read(id));
        saveToFile(channels);

    }
}
