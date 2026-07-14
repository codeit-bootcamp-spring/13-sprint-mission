package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class ChannelMapper {

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    protected ReadStatusRepository readStatusRepository;

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "participants", expression = "java(mapParticipants(channel))")
    @Mapping(target = "lastMessageAt", expression = "java(mapLastMessageAt(channel))")
    public abstract ChannelDto toDto(Channel channel);

    protected List<UserDto> mapParticipants(Channel channel) {
        if (!channel.getType().equals(ChannelType.PRIVATE)) {
            return null;
        }
        return readStatusRepository.findAllByChannelId(channel.getId())
                .stream()
                .map(readStatus -> {
                    User user = readStatus.getUser();
                    return userMapper.toDto(user, user.getStatus());
                })
                .collect(Collectors.toList());
    }

    protected Instant mapLastMessageAt(Channel channel) {
        Pageable latestOne = PageRequest.of(0, 1, Sort.by("createdAt").descending());
        return messageRepository.findAllByChannelId(channel.getId(), latestOne).stream()
                .findFirst()
                .map(Message::getCreatedAt)
                .orElse(null);
    }
}
