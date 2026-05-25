package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.util.*;

public class FileChannelService implements ChannelService {

    private final File channelFile;
    private final Map<UUID, Channel> channels;
    private Map<UUID, Channel> loadChannels()  {
        if (!channelFile.exists()) {
            return new HashMap<UUID, Channel>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(channelFile))) {
            return (Map<UUID, Channel>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<UUID, Channel>();
        }


    }
    private void saveChannels() {
        File parent = channelFile.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(channelFile))) {
            out.writeObject(channels);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public FileChannelService() {
        this.channelFile = new File("data/channel.ser");
        this.channels = loadChannels();
    }



    @Override
    public void create(Channel channel) {
        channels.put(channel.getId(), channel);
        saveChannels();
    }

    @Override
    public Channel findById(UUID id) {
        return channels.get(id);
    }

    @Override
    public Collection<Channel> findAll() {
        return channels.values();
    }

    @Override
    public void update(UUID id, String name, String description) {
        Channel channel = channels.get(id);
        if ( channel != null) {
            channel.update(name, description);
        }
        saveChannels();
    }

    @Override
    public void delete(UUID id) {
        channels.remove(id);
        saveChannels();
    }
}
