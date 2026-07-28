package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;

    public MessageResponse toDto(Message message) {
        if (message == null) {
            return null;
        }

        List<BinaryContentResponse> attachments = message.getAttachments() == null
                ? List.of()
                : message.getAttachments().stream()
                .map(binaryContentMapper::toDto)
                .toList();

        UUID channelId = message.getChannel() == null ? null : message.getChannel().getId();
        UUID senderId = message.getAuthor() == null ? null : message.getAuthor().getId();

        return new MessageResponse(
                message.getId(),
                channelId,
                senderId,
                message.getContent(),
                attachments,
                message.getCreatedAt(),
                message.getUpdatedAt(),
                userMapper.toDto(message.getAuthor())
        );
    }
}