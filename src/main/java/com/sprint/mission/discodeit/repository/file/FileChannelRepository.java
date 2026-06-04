package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class FileChannelRepository implements ChannelRepository {

    private final File channelFileRepo;
    private final Map<UUID, Channel> channelsRepo;
    private Map<UUID, Channel> loadChannelsRepo()  {
        if (!channelFileRepo.exists()) {
            return new HashMap<UUID, Channel>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(channelFileRepo))) {
            return (Map<UUID, Channel>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<UUID, Channel>();
        }


    }
    private void saveChannelsRepo() {
        File parent = channelFileRepo.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(channelFileRepo))) {
            out.writeObject(channelsRepo);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public FileChannelRepository() {
        this.channelFileRepo = new File("data/repository-channel.json");
        this.channelsRepo = loadChannelsRepo();
    }

    @Override
    public void save(Channel channel) {
        channelsRepo.put(channel.getId(), channel);
        saveChannelsRepo();
    }

    @Override
    public Channel findById(UUID id) {
        return channelsRepo.get(id);
    }

    @Override
    public Collection<Channel> findAll() {
        return new ArrayList<>(channelsRepo.values());
    }

    @Override
    public void delete(UUID id) {
        channelsRepo.remove(id);
        saveChannelsRepo();
    }
}
