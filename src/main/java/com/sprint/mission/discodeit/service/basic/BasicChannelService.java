package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    @Override
    public ChannelResponse createPublic(PublicChannelCreateRequest request) {
        Channel channel = new Channel(
                ChannelType.PUBLIC,
                request.getName(),
                request.getDescription()
        );

        channel = channelRepository.save(channel);

        return new ChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                null,
                null
        );
    }

    @Override
    public ChannelResponse createPrivate(
            PrivateChannelCreateRequest request
    ) {

        Channel channel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );

        channel = channelRepository.save(channel);

        for (UUID participantId : request.getParticipantIds()) {

            ReadStatus readStatus = new ReadStatus(
                    participantId,
                    channel.getId()
            );

            readStatusRepository.save(readStatus);
        }

        return new ChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                null,
                request.getParticipantIds()
        );
    }

    @Override
    public ChannelResponse find(UUID channelId) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Channel with id " + channelId + " not found"
                        )
                );

        Instant latestMessageAt = messageRepository
                .findByChannelId(channelId)
                .stream()
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> participantIds = null;

        if (channel.getType() == ChannelType.PRIVATE) {

            participantIds = readStatusRepository
                    .findByChannelId(channelId)
                    .stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        }

        return new ChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                latestMessageAt,
                participantIds
        );
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {

        return channelRepository.findAll()

                .stream()

                .filter(channel -> {

                    if (channel.getType() == ChannelType.PUBLIC) {
                        return true;
                    }

                    return readStatusRepository
                            .findByChannelId(channel.getId())
                            .stream()
                            .anyMatch(status ->
                                    status.getUserId().equals(userId)
                            );
                })

                .map(channel -> {

                    Instant latestMessageAt = messageRepository
                            .findByChannelId(channel.getId())
                            .stream()
                            .map(Message::getCreatedAt)
                            .max(Instant::compareTo)
                            .orElse(null);

                    List<UUID> participantIds = null;

                    if (channel.getType() == ChannelType.PRIVATE) {

                        participantIds = readStatusRepository
                                .findByChannelId(channel.getId())
                                .stream()
                                .map(ReadStatus::getUserId)
                                .toList();
                    }

                    return new ChannelResponse(
                            channel.getId(),
                            channel.getType(),
                            channel.getName(),
                            channel.getDescription(),
                            latestMessageAt,
                            participantIds
                    );
                })

                .toList();
    }

    @Override
    public ChannelResponse update(
            ChannelUpdateRequest request
    ) {

        Channel channel = channelRepository.findById(
                        request.getChannelId()
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Channel with id "
                                        + request.getChannelId()
                                        + " not found"
                        )
                );

        if (channel.getType() == ChannelType.PRIVATE) {

            throw new IllegalArgumentException(
                    "PRIVATE 채널은 수정할 수 없습니다."
            );
        }

        channel.update(
                request.getName(),
                request.getDescription()
        );

        channel = channelRepository.save(channel);

        Instant latestMessageAt = messageRepository
                .findByChannelId(channel.getId())
                .stream()
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        return new ChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                latestMessageAt,
                null
        );
    }

    @Override
    public void delete(UUID channelId) {

        if (!channelRepository.existsById(channelId)) {

            throw new NoSuchElementException(
                    "Channel with id "
                            + channelId
                            + " not found"
            );
        }

        readStatusRepository
                .findByChannelId(channelId)

                .forEach(readStatus ->

                        readStatusRepository.deleteById(
                                readStatus.getId()
                        )
                );

        messageRepository
                .findByChannelId(channelId)

                .forEach(message ->

                        messageRepository.deleteById(
                                message.getId()
                        )
                );

        channelRepository.deleteById(channelId);
    }
}
