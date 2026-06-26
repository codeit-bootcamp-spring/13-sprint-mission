package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.*;
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

    @Override
    public ChannelResponse createPublic(PublicChannelCreateRequest request) {
        User creator = userRepository.findById(request.creatorId());

        if (creator == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }

        Channel channel = new Channel(
                request.name(),
                request.description(),
                creator,
                ChannelType.PUBLIC
        );

        channelRepository.save(channel);

        return toResponse(channel);
    }

    @Override
    public ChannelResponse createPrivate(PrivateChannelCreateRequest request) {
        for (UUID participantId : request.participantIds()) {
            User user = userRepository.findById(participantId);

            if (user == null) {
                throw new IllegalArgumentException("존재하지 않는 유저입니다.");
            }
        }

        Channel channel = new Channel(
                null,
                null,
                null,
                ChannelType.PRIVATE
        );

        channelRepository.save(channel);

        for (UUID participantId : request.participantIds()) {

            ReadStatus readStatus = new ReadStatus(
                    participantId,
                    channel.getId(),
                    Instant.now()
            );

            readStatusRepository.save(readStatus);
        }

        return toResponse(channel);
    }

    @Override
    public ChannelResponse findById(UUID id) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            return null;
        }
        return toResponse(channel);
    }

    @Override
    public Collection<ChannelResponse> findAllByUserId(UUID userId) {
        List<ChannelResponse> responses = new ArrayList<>();

        for (Channel channel : channelRepository.findAll()) {
            if (channel.getType() == ChannelType.PUBLIC) {
                responses.add(toResponse(channel));
                continue;
            }

            if (channel.getType() == ChannelType.PRIVATE
                    && readStatusRepository.findByUserIdAndChannelId(userId, channel.getId()) != null) {
                responses.add(toResponse(channel));
            }
        }

        return responses;
    }

    @Override
    public ChannelResponse update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(request.name(), request.description());
        channelRepository.save(channel);

        return toResponse(channel);
    }

    @Override
    public void delete(UUID id) {

        Channel channel = channelRepository.findById(id);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        for (Message message : messageRepository.findAllByChannelId(id)) {
            messageRepository.delete(message.getId());
        }

        for (ReadStatus readStatus : readStatusRepository.findAllByChannelId(id)) {
            readStatusRepository.delete(readStatus.getId());
        }

        channelRepository.delete(id);
    }

    private ChannelResponse toResponse(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getNameDescription(),
                channel.getType(),
                findLastMessageAt(channel.getId()),
                findParticipantIds(channel)
        );
    }

    private Instant findLastMessageAt(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(Message::getCreateAt)
                .max(Instant::compareTo)
                .orElse(null);
    }

    private List<UUID> findParticipantIds(Channel channel) {
        if (channel.getType() == ChannelType.PUBLIC) {
            return List.of();
        }

        List<UUID> participantIds = new ArrayList<>();

        for (ReadStatus readStatus : readStatusRepository.findAllByChannelId(channel.getId())) {
            participantIds.add(readStatus.getUserId());
        }

        return participantIds;
    }
}
