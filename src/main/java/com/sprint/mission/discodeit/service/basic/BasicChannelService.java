package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sprint.mission.discodeit.entity.Channel.ChannelType.PUBLIC;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;


    @Override
    public ChannelResponse createPublicChannel(CreatePublicChannelRequest request) {
        Channel channel = new Channel(
                request.name(),
                PUBLIC,
                request.description()
        );
        channelRepository.save(channel);

        return ChannelResponse.from(
                channel,
                null,
                List.of()
        );
    }

    @Override
    public ChannelResponse createPrivateChannel(CreatePrivateChannelRequest request) {
        Channel channel = new Channel(
                null,
                Channel.ChannelType.PRIVATE,
                null
        );
        channelRepository.save(channel);

        for (UUID userId : request.userIds()) {
            ReadStatus readStatus = new ReadStatus(
                            UUID.randomUUID(),
                            userId, channel.getId()
            );
            readStatusRepository.save(readStatus);
        }

        return ChannelResponse.from(
                channel,
                null,
                request.userIds()
        );
    }

    @Override
    public ChannelResponse find(UUID id) {
        Channel channel = channelRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("채널을 찾을 수 없습니다."));;

        Instant latestMessageAt = messageRepository.findAllByChannelId(channel.getId())
                .stream().filter(message -> message.getChannelId().equals(id))
                .map(Message::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);

        List<UUID> participantIds = List.of();

        if (channel.getType() == Channel.ChannelType.PRIVATE) {
            participantIds = readStatusRepository.findAll()
                    .stream()
                    .filter(status ->
                            status.getChannelId().equals(id))
                    .map(ReadStatus::getUserId)
                    .toList();
        }

        return ChannelResponse.from(
                channel,
                latestMessageAt,
                participantIds
        );
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        Set<UUID> joinedChannelIds =
                readStatusRepository.findAll()
                        .stream().filter(status ->
                                status.getUserId().equals(userId))
                        .map(ReadStatus::getChannelId)
                        .collect(Collectors.toSet());

        return channelRepository.findAll()
                .stream()
                .filter(channel ->
                        channel.getType() == PUBLIC || joinedChannelIds.contains(channel.getId()))
                .map(channel -> {

                    Instant latestMessageAt = messageRepository.findAllByChannelId(channel.getId())
                            .stream()
                            .filter(message ->
                                    message.getChannelId().equals(channel.getId()))
                            .map(Message::getCreatedAt)
                            .max(Instant::compareTo)
                            .orElse(null);

                    List<UUID> participantIds = List.of();

                    if (channel.getType() == Channel.ChannelType.PRIVATE) {
                        participantIds = readStatusRepository.findAll()
                                .stream().filter(status -> status.getChannelId().equals(channel.getId()))
                                .map(ReadStatus::getUserId)
                                .toList();
                    }

                    return ChannelResponse.from(
                            channel,
                            latestMessageAt,
                            participantIds
                    );
                })

                .toList();
    }

    @Override
    public void update(UpdateChannelRequest request) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() ->
                        new IllegalArgumentException("채널을 찾을 수 없습니다."));;

        if (channel.getType() == Channel.ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.update(request.name(), PUBLIC, request.description());

        channelRepository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        // 해당 채널의 메시지 삭제
        messageRepository.findAllByChannelId(id)
                .forEach(message -> messageRepository.delete(message.getId()));

        // ReadStatus 삭제
        readStatusRepository.findAllByChannelId(id)
                .forEach(status -> readStatusRepository.delete(status.getId()));

        // 채널 삭제
        channelRepository.delete(id);
    }
}
