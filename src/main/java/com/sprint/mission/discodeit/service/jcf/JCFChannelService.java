package com.sprint.mission.discodeit.service.jcf;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    private final HashMap<UUID, Channel> data;

    private static class ChannelInstance {
        private static final JCFChannelService INSTANCE = new JCFChannelService();
    }

    private JCFChannelService() {
        data = new HashMap<>();
    }

    public static JCFChannelService getInstance() {
        return ChannelInstance.INSTANCE;
    }



    @Override
    public void createChannel(String name, String description, ChannelType type){
        Channel channel = new Channel(name, description, type);
        for (int i = 0; i < 3; i++){
            if (this.data.containsKey(channel
                    .getId())) channel
                    = new Channel(name, description, type);
        }
        this.data.put(channel.getId(),channel);
    }

    @Override
    public ArrayList<Channel> readChannel(UUID id){
        ArrayList<Channel> channel = new ArrayList<>();
        channel.add(this.data.get(id));
        return channel;
    }

    @Override
    public ArrayList<Channel> readChannelAll(){
        return new ArrayList<>(data.values());
    }

    @Override
    public void updateChannel(UUID id, String name,String description,ChannelType type){
        Channel cnl = data.get(id);
        cnl.setName(name);
        cnl.setDescription(description);
        cnl.setType(type);
        cnl.setUpdatedAt(System.currentTimeMillis());
    }

    @Override
    public void deleteChannel(UUID id){
        data.remove(id);
    }
}
