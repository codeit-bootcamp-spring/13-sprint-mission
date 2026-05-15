package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface ChannelService {

    void create(Channel channel);

    Channel read(UUID id);

    List<Channel> readAll();

    void update(UUID id, Channel channel);

    void delete(UUID id);


}
