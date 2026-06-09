package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    //PUBLIC 채널 생성
    @Override
    public ChannelResponse createPublicChannel(PublicChannelRequest request) {
        boolean duplicateCheck = channelRepository.findAll().stream()
                .anyMatch(channel -> request.name().equals(channel.getName()));
        if (duplicateCheck) {
            throw new IllegalArgumentException("이미 동일한 채널명이 존재합니다.");
        }
        Channel channel = new Channel(request.name(), request.description());
        channelRepository.save(channel);
        return new ChannelResponse(
                channel.getChannelId(),
                channel.getChannelType(),
                channel.getName(),
                channel.getDescription(),
                null,
                null
        );
    }
    //PRIVATE 채널 생성
    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE);
        channelRepository.save(channel);

        request.participantIds().forEach(userId -> {
            ReadStatus readStatus = new ReadStatus(userId, channel.getChannelId());
            readStatusRepository.save(readStatus);
        });
        return  new ChannelResponse(
                channel.getChannelId(),
                channel.getChannelType(),
                null,
                null,
                request.participantIds(),
                null
        );
    }

    @Override
    public ChannelResponse findByChannel(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));
        List<UUID> participantIds = null;
        if (channel.getChannelType().equals(ChannelType.PRIVATE)) {
            participantIds = readStatusRepository.findAllByChannelId(channelId)
                    .stream()
                    .map(ReadStatus::getUserId)
                    .collect(Collectors.toList());
        }

        Instant lastMessageAt = messageRepository.findAllByChannelId(channelId)
                .stream()
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new ChannelResponse(
                channel.getChannelId(),
                channel.getChannelType(),
                channel.getName(),
                channel.getDescription(),
                participantIds,
                lastMessageAt
        );
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<Channel> publicChannels = channelRepository.findAll()
                .stream()
                .filter(channel -> channel.getChannelType().equals(ChannelType.PUBLIC))
                .collect(Collectors.toList());

        List<Channel> privateChannels = readStatusRepository.findAllByUserId(userId)
                .stream()
                .map(readStatus -> channelRepository.findById(readStatus.getChannelId())
                        .orElseThrow())
                .collect(Collectors.toList());

        List<Channel> allChannels = new ArrayList<>();
        allChannels.addAll(publicChannels);
        allChannels.addAll(privateChannels);

        return allChannels.stream()
                .map(channel -> findByChannel(channel.getChannelId()))
                .collect(Collectors.toList());

    }

    @Override
    public ChannelResponse updateChannel(UUID ChannelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(ChannelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널 입니다."));
        if (channel.getChannelType().equals(ChannelType.PRIVATE)) {
            throw new IllegalArgumentException("비공개 채널은 수정할 수 없습니다.");
        }
        if (request.name() != null && !request.name().isBlank()) channel.updateChannel(request.name());
        if (request.description() != null && !request.description().isBlank()) channel.updateChannelDescription(request.description());

        channelRepository.save(channel);
        return findByChannel(ChannelId);
    }

    @Override
    public void deleteChannel(UUID ChannelId) {
        Channel channel = channelRepository.findById(ChannelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다."));

        messageRepository.findAllByChannelId(channel.getChannelId())
                        .forEach(message -> messageRepository.deleteById(message.getMessageId()));
        readStatusRepository.deleteByChannelId(channel.getChannelId());
        channelRepository.deleteById(channel.getChannelId());
    }
}
