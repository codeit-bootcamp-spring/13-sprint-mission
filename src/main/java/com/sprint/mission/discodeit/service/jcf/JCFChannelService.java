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
    private final HashMap<UUID,ArrayList<UUID>> userList;

    private static class ChannelInstance {
        private static final JCFChannelService INSTANCE = new JCFChannelService();
    }

    private JCFChannelService() {
        data = new HashMap<>();
        userList = new HashMap<>();
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
        this.userList.put(channel.getId(),new ArrayList<>());
    }

    @Override
    public ArrayList<Channel> readChannel(UUID id){
        if(!this.data.containsKey(id)) JCFException.throwRuntimeError("Channel dos not exist.");
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
        Channel cnl = data.remove(id);
        if ( cnl == null) JCFException.throwRuntimeError("Channel dos not exist.");
    }


    public void join(UUID userID,UUID channelID){
        userList.get(channelID).add(userID);
    }

    public void quit(UUID userID,UUID channelID)
    {
        userList.get(channelID).remove(userID);
    }
}
