package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
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

        User authorEntity = message.getAuthor();

        UserDto author = authorEntity != null
                ? userMapper.toDto(
                authorEntity,
                isOnline(authorEntity.getId())
        )
                : null;

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
