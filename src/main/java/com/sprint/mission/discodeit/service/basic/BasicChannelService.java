package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final ChannelMapper channelMapper;

    @Override
    @Transactional
    public ChannelDto createPublic(PublicChannelCreateRequest request) {
        log.info("Creating public channel: name={}", request.name());

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
    @Transactional
    public ChannelDto createPrivate(PrivateChannelCreateRequest request) {
        log.info("Creating private channel: participantCount={}", request.participantIds().size());
        List<User> participants = new ArrayList<>();

        for (UUID participantId : request.participantIds()) {
            User user = userRepository.findById(participantId)
                    .orElseThrow(() -> new UserNotFoundException(participantId));

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
                .orElseThrow(() -> new ChannelNotFoundException(id));

        return channelMapper.toDto(channel);
    }

    @Override
    public Collection<ChannelDto> findAllByUserId(UUID userId) {
        List<ChannelDto> responses = new ArrayList<>();

        channelRepository.findAllByType(ChannelType.PUBLIC).stream()
                .map(channelMapper::toDto)
                .forEach(responses::add);

        readStatusRepository.findAllByUser_Id(userId).stream()
                .map(ReadStatus::getChannel)
                .filter(channel -> channel.getType() == ChannelType.PRIVATE)
                .map(channelMapper::toDto)
                .forEach(responses::add);

        return responses;
    }

    @Override
    @Transactional
    public ChannelDto update(UUID channelId, ChannelUpdateRequest request) {
        log.info("Updating channel: channelId={}", channelId);
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));

        if (channel.getType() == ChannelType.PRIVATE) {
            log.warn("Rejected private channel update: channelId={}", channelId);
            throw new PrivateChannelUpdateException(channelId);
        }

        channel.update(request.name(), request.description());
        channelRepository.save(channel);

        return channelMapper.toDto(channel);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting channel: channelId={}", id);
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new ChannelNotFoundException(id));

        messageRepository.deleteAll(messageRepository.findAllByChannel_Id(id));

        readStatusRepository.deleteAll(readStatusRepository.findAllByChannel_Id(id));

        channelRepository.delete(channel);
    }

}
