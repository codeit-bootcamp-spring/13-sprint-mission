package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
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
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto create(CreateChannelRequest request) {
        Channel channel = new Channel(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        Channel savedChannel = channelRepository.save(channel);

        return channelMapper.toDto(savedChannel);
    }

    @Override
    public ChannelDto find(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Channel not found: " + id
                        )
                );

        return channelMapper.toDto(channel);
    }

    @Override
    public List<ChannelDto> findAll() {
        return channelRepository.findAll()
                .stream()
                .map(channelMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ChannelDto update(
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

        return channelMapper.toDto(channel);
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