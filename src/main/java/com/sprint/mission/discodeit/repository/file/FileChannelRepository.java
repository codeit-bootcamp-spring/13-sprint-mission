package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.nio.file.Path;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class FileChannelRepository extends FileBaseRepository implements ChannelRepository {

    public FileChannelRepository(Path path) {
        super(path);
    }

    @Override
    public void create(String name, String description, ChannelType type) {
        HashMap<UUID, Channel> channelList = this.load();
        Channel channel = new Channel(name, description, type);
        for (int i = 0; i < 3; i++){
            if (channelList.containsKey(channel.getId()))
                channel = new Channel(name, description, type);
        }
        channelList.put(channel.getId(),channel);
        this.save(channelList);
    }

    @Override
    public ArrayList<Channel> select (Predicate<Channel> fn) {
        HashMap<UUID, Channel> channelList = this.load();
        return new ArrayList<>(channelList.values().stream()
                .filter(fn)
                .toList());
    }

    @Override
    public void update(UUID pcnl, String name,String description,ChannelType type) {
        HashMap<UUID, Channel> channelList = this.load();
        Channel cnl = channelList.get(pcnl);
        cnl.setName(name);
        cnl.setDescription(description);
        cnl.setType(type);
        cnl.setUpdatedAt(System.currentTimeMillis());
        this.save(channelList);
    }

    @Override
    public void delete(UUID cnl) {
        HashMap<UUID, Channel> channelList = this.load();
        channelList.remove(cnl);
        this.save(channelList);
    }
}
