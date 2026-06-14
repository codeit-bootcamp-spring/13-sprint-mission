package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Repository
public class FileChannelRepository implements ChannelRepository {
    private final Path path;

    public FileChannelRepository(@Value("${file.path.channel}") String path) {
        this.path = Paths.get(path);
    }

    private void saveFile(List<Channel> channels) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(channels));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Channel> loadFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Channel create(Channel channel) {
        List<Channel> channels = loadFile();
        channels.add(channel);
        saveFile(channels);
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        List<Channel> channels = loadFile();
        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> readAll() {
        return loadFile();
    }

    @Override
    public void update(Channel channel) {
        List<Channel> channels = loadFile();

        for (Channel c : channels) {
            if (c.getId().equals(channel.getId())) {
                c.update(channel.getType(), channel.getChannelName(), channel.getDescription());
                break;
            }
        }
        saveFile(channels);
    }

    @Override
    public void delete(UUID id) {
        List<Channel> channels = loadFile();

        Iterator<Channel> iterator = channels.iterator();
        while (iterator.hasNext()) {
            Channel channel = iterator.next();
            if (channel.getId().equals(id)) {
                iterator.remove();
                break;
            }
        }

        saveFile(channels);
    }
}
