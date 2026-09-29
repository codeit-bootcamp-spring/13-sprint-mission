package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserMapper userMapper;
    private final JwtRegistry jwtRegistry;

    public ChannelDto toDto(Channel channel) {
        List<UserDto> participants = readStatusRepository.findAllByChannelId(channel.getId()).stream()
                .map(readStatus -> userMapper.toDto(readStatus.getUser(), isOnline(readStatus.getUser().getId())))
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

    // 로그인 여부 판단 메서드
    private boolean isOnline(UUID userId) {
        return jwtRegistry.hasActiveJwtInformationByUserId(userId);
    }

}
