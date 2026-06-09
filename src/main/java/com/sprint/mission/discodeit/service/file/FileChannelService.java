package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;


public class FileChannelService implements ChannelService {

    private final ChannelRepository repository;

    public FileChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {

        Channel channel = new Channel(
                request.name(),
                request.description(),
                "PUBLIC"
        );

        repository.save(channel);

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                null,
                null
        );
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {

        Channel channel = new Channel(
                null,
                null,
                "PRIVATE"
        );

        repository.save(channel);

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                null,
                request.memberIds()
        );
    }

    @Override
    public ChannelResponse findById(UUID id) {

        Channel channel = repository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Channel not found"));

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                null,
                null
        );
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {

        return repository.findAll().stream()
                .map(channel -> new ChannelResponse(
                        channel.getId(),
                        channel.getName(),
                        channel.getDescription(),
                        channel.getType(),
                        null,
                        null
                ))
                .toList();
    }

    @Override
    public ChannelResponse update(ChannelUpdateRequest request) {

        Channel channel = repository.findById(request.id())
                .orElseThrow(() ->
                        new NoSuchElementException("Channel not found"));

        channel.update(
                request.name(),
                request.description()
        );

        repository.save(channel);

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                null,
                null
        );
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}
