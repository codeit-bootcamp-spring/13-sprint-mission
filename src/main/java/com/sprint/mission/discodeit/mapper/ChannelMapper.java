package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final UserMapper userMapper;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;


    public ChannelDto toDto(Channel channel) {

        List<UserDto> participants = null;
        if (channel.getType().equals(ChannelType.PRIVATE)){
            participants = readStatusRepository.findAllByChannelId(channel.getId())
                    .stream()
                    .map(readStatus -> {
                        User user = readStatus.getUser();
                        return userMapper.toDto(user, user.getStatus());
                    })
                    .collect(Collectors.toList());
        }

        Pageable latestOne = PageRequest.of(0, 1, Sort.by("createdAt").descending());
        Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId(), latestOne).stream()
                .findFirst()
                .map(Message::getCreatedAt)
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
