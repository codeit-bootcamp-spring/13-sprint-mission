package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FileChannelRepository implements ChannelRepository {

    private final Path filePath = Paths.get(System.getProperty("user.dir"), "channels.ser");

    @SuppressWarnings("unchecked")
    private List<Channel> readFile() {
        if(!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
        ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private void saveFile(List<Channel> channels) {
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(channels);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    public Channel create(Channel channel) {
        List<Channel> channels = readFile();
        channels.add(channel);
        saveFile(channels);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        return readFile().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Channel> findAll() {
        return readFile();
    }

    @Override
    public void update(Channel requestChannel) {
        List<Channel> channels = readFile();
        channels.replaceAll(c -> c.getId()
                .equals(requestChannel.getId())
                ? requestChannel : c);
        saveFile(channels);
    }

    @Override
    public void delete(UUID id) {
        List<Channel> channels = readFile();
        channels.removeIf(c -> c.getId().equals(id));
        saveFile(channels);
    }
}
