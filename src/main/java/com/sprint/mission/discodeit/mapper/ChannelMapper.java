package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserMapper userMapper;

    public ChannelDto toDto(Channel channel) {
        List<UserDto> participants = getParticipants(channel);
        Instant lastMessageAt = getLastMessageAt(channel.getId());

        return new ChannelDto(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                participants,
                lastMessageAt
        );
    }

    public ChannelDto toDto(Channel channel, List<UserDto> participants, Instant lastMessageAt) {
        return new ChannelDto(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                participants,
                lastMessageAt);
    }

    private List<UserDto> getParticipants(Channel channel) {
        List<UserDto> participants = new ArrayList<>();
        if(channel.getType()== ChannelType.PUBLIC) return participants;

        List<ReadStatus> readStatuses = readStatusRepository.findAllByChannel_Id(channel.getId());
        for (ReadStatus readStatus : readStatuses) {
            participants.add(userMapper.toDto(readStatus.getUser()));
        }
        return participants;
    }

    private Instant getLastMessageAt(UUID id) {
        List<Message> messages = messageRepository.findAllByChannel_Id(id, Pageable.unpaged()).getContent();
        Instant lastMessageAt = null;
        for (Message message : messages) {
            if(lastMessageAt==null || message.getUpdatedAt().isAfter(lastMessageAt)) {
                lastMessageAt = message.getUpdatedAt();
            }
        }
        return lastMessageAt;
    }

}
