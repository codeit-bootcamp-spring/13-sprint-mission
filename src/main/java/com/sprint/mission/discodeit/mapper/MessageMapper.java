package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.security.JwtRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;
    private final JwtRegistry jwtRegistry;

    public MessageDto toDto(Message message) {

        UserDto author = userMapper.toDto(
                message.getAuthor(),
                isOnline(message.getAuthor().getId())
        );

        List<BinaryContentDto> attachments = message.getAttachments().stream()
                .map(binaryContentMapper::toDto)
                .toList();

        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannel().getId(),
                author,
                attachments
        );
    }

    // 로그인 여부 판단 메서드
    private boolean isOnline(UUID userId) {
        return jwtRegistry.hasActiveJwtInformationByUserId(userId);
    }
}
