package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
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
    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        if (channel == null) {
            return null;
        }

        return new ChannelDto(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getNameDescription(),
                findParticipants(channel),
                findLastMessageAt(channel.getId())
        );
    }

    private Instant findLastMessageAt(UUID channelId) {
        return messageRepository.findTopByChannel_IdOrderByCreatedAtDesc(channelId)
                .map(Message::getCreateAt)
                .orElse(null);
    }

    private List<UserDto> findParticipants(Channel channel) {
        if (channel.getType() == ChannelType.PUBLIC) {
            return List.of();
        }

        return readStatusRepository.findAllByChannel_Id(channel.getId()).stream()
                .map(readStatus -> userMapper.toDto(readStatus.getUser()))
                .toList();
    }
}