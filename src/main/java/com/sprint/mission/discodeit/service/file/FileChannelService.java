package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {

    private final ChannelRepository repository;

    public FileChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public Channel create(Channel channel) {
        return repository.save(channel);
    }

    @Override
    public Channel findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Channel> findAll() {
        return repository.findAll();
    }

    @Override
    public void update(UUID id, String name, String description, Channel.ChannelType type) {
        Channel channel = repository.findById(id);

        if (channel == null) {
            throw new IllegalArgumentException("Channel not found");
        }
        channel.updateName(name);
        channel.updateDescription(description);
        channel.updateType(type);
        repository.save(channel);

    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);

    }
}
