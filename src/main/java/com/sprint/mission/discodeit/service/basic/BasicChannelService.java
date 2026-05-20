package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFSelectFilter;
import com.sprint.mission.discodeit.service.ChannelService;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;

public class BasicChannelService implements ChannelService {
    private final FileChannelRepository fcr = new FileChannelRepository(Paths.get("channel.ser"));
    @Override
    public void createChannel(String name, String description, ChannelType type){
        fcr.create(name, description, type);
    }

    @Override
    public ArrayList<Channel> readChannel(UUID id) {
        JCFSelectFilter<Channel> flt = (c) -> c.getId().equals(id);
        return fcr.select(flt);
    }

    @Override
    public ArrayList<Channel> readChannelAll() {
        JCFSelectFilter<Channel> flt = (c) -> true;
        return fcr.select(flt);
    }

    @Override
    public void updateChannel(UUID id, String name, String description, ChannelType type){
        fcr.update(id, name, description, type);
    }

    @Override
    public void deleteChannel(UUID id) {
        fcr.delete(id);
    }
}
