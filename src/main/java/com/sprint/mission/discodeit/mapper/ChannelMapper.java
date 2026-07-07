package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.JAPReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChannelMapper {
    private final JPAMessageRepository messageRepository;
    private final UserMapper userMapper;
    private final JAPReadStatusRepository  readStatusRepository;

    public ChannelDto toDto(Channel channel) {


        return new ChannelDto(
                channel.getId()
                ,channel.getType()
                ,channel.getName()
                ,channel.getDescription()
                ,userDtoFromChannel(channel)
                ,lastMessageAt(channel)
        );
    }

    private List<UserDto> userDtoFromChannel(Channel channel) {
        return readStatusRepository.findByChannelId(channel.getId())
                .stream().map(
                        rs -> userMapper.toDto(rs.getUser())
                ).toList();
    }

    private Instant lastMessageAt(Channel channel) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createdAt")));
        return messageRepository.findByChannelIdOrderByCreatedAtDesc(channel.getId(),pageable)
                .stream()
                .findFirst()
                .map(Message::getCreatedAt)
                .orElse(null);
    }

}
