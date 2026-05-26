package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFChannelService implements ChannelService {

   private final ChannelRepository repository;

   public JCFChannelService(ChannelRepository repository) {
       this.repository = repository;
   }

   @Override
    public void create(Channel channel) {
        repository.create(channel);
    }

    @Override
    public Channel read(UUID id) {
        return repository.read(id);
    }

    @Override
    public List<Channel> readAll() {
        return repository.readAll();
    }

    @Override
    public void update(UUID id, Channel channel) {
        repository.update(id, channel);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}
