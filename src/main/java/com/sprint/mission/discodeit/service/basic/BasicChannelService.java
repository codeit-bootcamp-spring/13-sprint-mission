package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.UUID;

public class BasicChannelService implements ChannelService {
    private final ChannelRepository fcr;

    public BasicChannelService(ChannelRepository chn) {
        fcr = chn;
    }

    @Override
    public void createChannel(String name, String description, ChannelType type){
        fcr.create(name, description, type);
    }

    @Override
    public ArrayList<Channel> readChannel(UUID id) {
        return fcr.select((c) -> c.getId().equals(id));
    }

    @Override
    public ArrayList<Channel> readChannelAll() {
        return fcr.select((c) -> true);
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
