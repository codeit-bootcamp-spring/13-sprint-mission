package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.domain.*;

import java.time.*;
import java.util.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class ChannelMapper {

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    protected ReadStatusRepository readStatusRepository;

    @Autowired
    protected UserStatusRepository userStatusRepository;

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "participants", source = ".")
    @Mapping(target = "lastMessageAt", source = ".")
    public abstract ChannelDto toDto(Channel channel);

    protected List<UserDto> mapParticipants(Channel channel) {
        if (channel == null || channel.getType() != ChannelType.PRIVATE) {
            return null;
        }

        return readStatusRepository.findAllByChannelId(channel.getId())
                .stream()
                .map(readStatus -> {
                    User user = readStatus.getUser();
                    return userMapper.toDto(user, user.getUserStatus());
                })
                .toList();
    }


    protected Instant mapLastMessageAt(Channel channel) {
        Pageable latestOne = PageRequest.of(0, 1, Sort.by("createdAt").descending());

        return messageRepository.findByChannelId(channel.getId(), latestOne)
                .getContent()
                .stream()
                .findFirst()
                .map(Message::getCreatedAt)
                .orElse(null);
    }
}
