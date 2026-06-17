package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    public BasicChannelService(ChannelRepository channelRepository,
                               ReadStatusRepository readStatusRepository,
                               MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.readStatusRepository = readStatusRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public ChannelResponse createPublicChannel(PublicChannelCreateRequest request) {
        Channel channel = new Channel(request.name(), request.description(), Channel.ChannelType.PUBLIC);
        channelRepository.save(channel);

        return convertToResponse(channel);
    }

    @Override
    public ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request) {
        Channel channel = new Channel(null, null, Channel.ChannelType.PRIVATE);
        channelRepository.save(channel);

        for (UUID userId : request.memberIds()) {
            ReadStatus readStatus = ReadStatus.builder()
                    .id(UUID.randomUUID())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .userId(userId)
                    .channelId(channel.getId())
                    .readAt(Instant.now())
                    .build();


            readStatusRepository.save(readStatus);
        }

        return convertToResponse(channel);
    }

    @Override
    public ChannelResponse findById(UUID id) {
        Optional<Channel> channel = channelRepository.findById(id);

        if (channel.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        return convertToResponse(channel.orElse(null));
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<Channel> allChannels = channelRepository.findAll();
        List<ChannelResponse> accessibleChannels = new ArrayList<>();

        for (Channel channel : allChannels) {
            if ("PUBLIC".equals(channel.getType())) {
                accessibleChannels.add(convertToResponse(channel));
            } else if ("PRIVATE".equals(channel.getType())) {
                boolean isParticipating = readStatusRepository.findAll().stream()
                        .anyMatch(rs -> rs.getUserId().equals(userId)
                        && rs.getChannelId().equals(channel.getId()));

                if (isParticipating) {
                    accessibleChannels.add(convertToResponse(channel));
                }
            }
        }

        return accessibleChannels;
    }

    @Override
    public ChannelResponse update(UUID id, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));

        if ("PRIVATE".equals(channel.getType())) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }
        channel.update(request.name(), request.description());

        return convertToResponse(channel);
    }

    @Override
    public void delete(UUID id) {
        List<Message> messages = messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(id))
                .toList();
        for (Message message : messages) {
            messageRepository.delete(message.getId());
        }
        List<ReadStatus> readStatuses = readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(id))
                .toList();
        for (ReadStatus readStatus : readStatuses) {
            readStatusRepository.delete(readStatus.getId());
        }
        channelRepository.delete(id);
    }

    private ChannelResponse convertToResponse(Channel channel) {
        Instant lastMessageAt = messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channel.getId()))
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> memberIds = null;
        if ("PRIVATE".equals(channel.getType())) {
            memberIds = readStatusRepository.findAll().stream()
                    .filter(rs -> rs.getChannelId().equals(channel.getId()))
                    .map(ReadStatus::getUserId)
                    .collect(Collectors.toList());
        }

        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(), lastMessageAt, memberIds);
    }
}
