package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MessageMapper {

    public MessageResponse toDto(Message message) {
        if (message == null) {
            return null;
        }

        List<UUID> attachmentIds = message.getAttachments().stream()
                .map(BinaryContent::getId)
                .toList();

        return new MessageResponse(
                message.getId(),
                message.getCreateAt(),
                message.getUpdateAt(),
                message.getContent(),
                message.getChannel().getId(),
                message.getAuthor().getId(),
                attachmentIds
        );
    }
}