package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        List<UserDto> participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(readStatus -> userMapper.toDto(readStatus.getUser()))
                .toList();

        Instant lastMessageAt = null;
        Message message = messageRepository.findAllByChannelId(channel.getId()).stream()
                .max(Comparator.comparing(Message::getCreatedAt))
                .orElse(null);

        if (message != null) {
            lastMessageAt = message.getCreatedAt();
        }

        return new ChannelDto(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                participants,
                lastMessageAt
        );
    }

}
