package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    public ChannelResponse toDto(Channel channel) {
        if (channel == null) {
            return null;
        }

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
        return messageRepository.findAllByChannel_Id(channelId).stream()
                .map(Message::getCreateAt)
                .max(Instant::compareTo)
                .orElse(null);
    }

    private List<UUID> findParticipantIds(Channel channel) {
        if (channel.getType() == ChannelType.PUBLIC) {
            return List.of();
        }

        return readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
                .map(readStatus -> readStatus.getUser().getId())
                .toList();
    }
}