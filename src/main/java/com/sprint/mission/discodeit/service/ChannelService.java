package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.*;

import java.nio.file.*;
import java.util.*;

public interface ChannelService {

    Channel create(String name, String description, ChannelType type);

    Channel read(UUID id);

    List<Channel> readAll();

    Channel update(UUID id, String name, String description, ChannelType type);

    void delete(UUID id);


}
