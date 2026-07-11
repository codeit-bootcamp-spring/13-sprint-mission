package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final ChannelMapper channelMapper;

    @Override
    public ChannelDto createPublic(PublicChannelCreateRequest request) {

        Channel channel = new Channel(
                request.name(),
                request.description(),
                null,
                ChannelType.PUBLIC
        );

        channelRepository.save(channel);

        return channelMapper.toDto(channel);
    }

    @Override
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        List<User> participants = new ArrayList<>();

        for (UUID participantId : request.participantIds()) {
            User user = userRepository.findById(participantId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

            participants.add(user);
        }

        Channel channel = new Channel(
                null,
                null,
                null,
                ChannelType.PRIVATE
        );

        channelRepository.save(channel);

        for (User participant : participants) {
            ReadStatus readStatus = new ReadStatus(
                    participant,
                    channel,
                    Instant.now()
            );

            readStatusRepository.save(readStatus);
        }

        return channelMapper.toDto(channel);
    }

    @Override
    public ChannelDto findById(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        return channelMapper.toDto(channel);
    }

    @Override
    public Collection<ChannelDto> findAllByUserId(UUID userId) {
        List<ChannelDto> responses = new ArrayList<>();

        for (Channel channel : channelRepository.findAll()) {
            if (channel.getType() == ChannelType.PUBLIC) {
                responses.add(channelMapper.toDto(channel));
                continue;
            }

            if (channel.getType() == ChannelType.PRIVATE
                    && readStatusRepository.findByUser_IdAndChannel_Id(userId, channel.getId()).isPresent()) {
                responses.add(channelMapper.toDto(channel));
            }
        }

        return responses;
    }

    @Override
    public ChannelDto update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(request.name(), request.description());
        channelRepository.save(channel);

        return channelMapper.toDto(channel);
    }

    @Override
    public void delete(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        messageRepository.deleteAll(messageRepository.findAllByChannel_Id(id));

        readStatusRepository.deleteAll(readStatusRepository.findAllByChannel_Id(id));

        channelRepository.delete(channel);
    }

}
