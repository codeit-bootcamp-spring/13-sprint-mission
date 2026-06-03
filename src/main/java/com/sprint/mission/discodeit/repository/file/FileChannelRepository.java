package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class FileChannelRepository implements ChannelRepository {
    private final String filePath = "channels.dat";
    private List<Channel> channels = new ArrayList<>();

    public FileChannelRepository() {
        load();
    }

    //@Override
    public Channel save(Channel channel) {
        channels.add(channel);
        saveToFile();
        return channel;
    }

    //@Override
    public Channel findByld(UUID id){
        for (Channel channel : channels) {
            if (channel.getId().equals(id))
                return channel;
        }
        return null;
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("uncheked")
    private void load() {
        File file = new File(filePath);
        if (!file.exists()) {
            channels = new ArrayList<>();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            channels = (List<Channel>) ois.readObject();
        } catch (Exception e) {
            channels = new ArrayList<>();
        }
    }

}