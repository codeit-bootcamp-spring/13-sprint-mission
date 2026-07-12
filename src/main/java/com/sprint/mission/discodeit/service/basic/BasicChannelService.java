package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    @Override
    @Transactional
    public Channel create(CreateChannelRequest request) {
        Channel channel = new Channel(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        return channelRepository.save(channel);
    }

    @Override
    public Channel find(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Channel not found: " + id
                        )
                );
    }

    @Override
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    @Transactional
    public Channel update(
            UUID id,
            UpdateChannelRequest request
    ) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Channel not found: " + id
                        )
                );

        channel.update(
                channel.getType(),
                request.getName(),
                request.getDescription()
        );

        return channel;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Channel not found: " + id
                        )
                );

        channelRepository.delete(channel);
    }
}