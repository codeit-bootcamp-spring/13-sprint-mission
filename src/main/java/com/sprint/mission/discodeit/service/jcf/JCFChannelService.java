package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {

    private final ChannelRepository repository;

    public JCFChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public Channel create(Channel channel) {
        return repository.save(channel);
    }

    @Override
    public Channel find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Channel not found"));
    }

    @Override
    public List<Channel> findAll() {
        return repository.findAll();
    }

    @Override
    public Channel update(UUID id, String name, String description) {

        Channel channel = find(id);

        channel.update(name, description);

        return repository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}