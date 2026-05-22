package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FileChannelService extends FileBase implements ChannelService{

    public FileChannelService(Path path) {
        super(path);
    }

    @Override
    public void createChannel(String name, String description, ChannelType type){

        // save logic
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
    public ArrayList<Channel> readChannel(UUID id){
        HashMap<UUID, Channel> channelList = this.load();
        ArrayList<Channel> channel = new ArrayList<>();
        channel.add(channelList.get(id));
        return channel;
    }

    @Override
    public ArrayList<Channel> readChannelAll(){
        HashMap<UUID, Channel> channelList = this.load();
        return new ArrayList<>(channelList.values());
    }

    @Override
    public void updateChannel(UUID id, String name,String description,ChannelType type){
        HashMap<UUID, Channel> channelList = this.load();
        Channel cnl = channelList.get(id);
        cnl.setName(name);
        cnl.setDescription(description);
        cnl.setType(type);
        cnl.setUpdatedAt(System.currentTimeMillis());
        this.save(channelList);
    }

    @Override
    public void deleteChannel(UUID id){
        HashMap<UUID, Channel> channelList = this.load();
        Channel cnl = channelList.remove(id);
        this.save(channelList);
    }



}
