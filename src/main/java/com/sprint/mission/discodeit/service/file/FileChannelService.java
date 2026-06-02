package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;


public class FileChannelService implements ChannelService {

    private final ChannelRepository repository;

    public FileChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public Channel create(Channel.ChannelType type, String name, String description) {
        Channel channel = new Channel(name, description, type);
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
    public Channel update(UUID channelId, String newName, String newDescription) {
        Channel channel = repository.findById(channelId);

        if (channel == null) {
            throw new IllegalArgumentException("Channel not found");
        }
        channel.updateName(newName);
        channel.updateDescription(newDescription);
        repository.save(channel);

        return channel;
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);

    }
}
