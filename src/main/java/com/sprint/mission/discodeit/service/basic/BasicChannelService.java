package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepesitory;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
//채널 관련 비즈니스 로직을 담당하는 Service 계층
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository; //채널 저장소 객체
    private final ReadStatusRepesitory readStatusRepesitory;
    private final MessageRepository messageRepository;
    private final ChannelService channelService;

    @Override //채널 생성
    public Channel create(PublicChannelCreateRequest request) {
        String name = request.name();
        String description = request.description();
        Channel channel = new Channel(ChannelType.PUBLIC, name, description);
        return channelRepository.save(channel);
    }
    @Override
    public Channel create(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);
        Channel createChannel = channelRepository.save(channel);

        request.participantIds().stream()
                .map(userId -> new ReadStatus(userId, createChannel.getId(), Instant.MIN))
                .forEach(readStatusRepesitory::save);
        return createChannel;
    }

    @Override //채널 단건조회
    public ChannelDto find(UUID channelId) {
        return channelRepository.findById(channelId).map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));
    }

    @Override //전체 채널 조회
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<UUID> mySubscribedChannelIds = readStatusRepesitory.findAllByUserId(userId).stream()
                .map(ReadStatus::getChannelId).toList();
        return channelRepository.findAll().stream()
                .filter(channel -> channel.getType().equals(ChannelType.PUBLIC) || mySubscribedChannelIds.contains(channel.getId()))
                .map(this::toDto).toList();
    }

    @Override //채널 수정
    public Channel update(UUID channelId, PublicChannelUpdateRequest request) {
        String newName = request.newName();
        String newDecription = request.newDescription();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            throw new IllegalArgumentException("Private channels can't be updated");
        }
        channel.update(newName, newDecription);
        return channelRepository.save(channel);
    }

    @Override //채널 삭제
    public void delete(UUID channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new NoSuchElementException("Channel with id " + channelId + " not found"));
        messageRepository.deleteAllByChannelId(channel.getId());
        readStatusRepesitory.deleteAllByChannelId(channel.getId());
        channelRepository.deleteById(channelId);
    }

    private ChannelDto toDto(Channel channel) {
        Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId())
                .stream().sorted(Comparator.comparing(Message::getCreatedAt).reversed())
                .map(Message::getChannelId)
                .limit(1)
                .findFirst()
                .orElse(Instant.MIN);

        List<UUID> participantIds = new ArrayList<>();
        if (channel.getType().equals(ChannelType.PRIVATE)) {
            readStatusRepesitory.findAllByChannelId(channel.getId())
                    .stream().map(ReadStatus::getUserId)
                    .forEach(participantIds::add);
        }

        return new ChannelDto(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                lastMessageAt,
                participantIds
        );

    }
}