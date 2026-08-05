package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

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

        List<UserDto> participants =
                readStatusRepository.findAllByChannelId(channel.getId())
                        .stream()
                        .map(ReadStatus::getUser)
                        .distinct()
                        .map(userMapper::toDto)
                        .toList();

        Instant lastMessageAt =
                messageRepository
                        .findTopByChannelIdOrderByCreatedAtDesc(
                                channel.getId()
                        )
                        .map(message -> message.getCreatedAt())
                        .orElse(null);

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