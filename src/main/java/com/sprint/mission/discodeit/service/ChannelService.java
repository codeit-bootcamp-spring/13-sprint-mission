package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;


import java.util.ArrayList;
import java.util.UUID;

public interface ChannelService {
    void createChannel(String name, String description, ChannelType type);
    ArrayList<Channel> getChannelById(UUID id);
    ArrayList<Channel> getChannelList();
    void updateChannelInfo(UUID id, String name,String description,ChannelType type);
    void deleteChannel(UUID id);
}
