package com.sprint.mission.discodeit.service.jcf;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.JCFException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    private final HashMap<UUID, Channel> data;
    // private final

    public JCFChannelService() {
        data = new HashMap<>();
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
        if(!this.data.containsKey(id)) JCFException.throwRuntimeError("Message dos not exist.");
        ArrayList<Channel> channel = new ArrayList<>();
        channel.add(this.data.get(id));
        return channel;
    }

    @Override
    public ArrayList<Channel> readChannelAll(){
        ArrayList<Channel> res = new ArrayList<>(data.values());
        return res;
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
        Channel cnl = data.remove(id);
        if ( cnl == null) JCFException.throwRuntimeError("Channel dos not exist.");
    }
}
