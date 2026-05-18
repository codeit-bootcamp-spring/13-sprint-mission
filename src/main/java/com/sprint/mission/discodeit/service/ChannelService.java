package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;


import java.util.ArrayList;
import java.util.UUID;

public interface ChannelService {
    void createChannel(String name, String description, ChannelType type);
    ArrayList<Channel> readChannel(UUID id);
    ArrayList<Channel> readChannelAll();
    void updateChannel(UUID id, String name,String description,ChannelType type);
    void deleteChannel(UUID id);
}
