package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public record MessageResponse(
        UUID id,
        UUID authorId,
        UUID channelId,
        String content

) {

    public record AttachmentResponse(
            UUID id,
            String fileName,
            String contentType
    ) {
        public static AttachmentResponse from(BinaryContent binaryContent) {
            return new AttachmentResponse(
                    binaryContent.getId(),
                    binaryContent.getFileName(),
                    binaryContent.getContentType()
            );
        }
    }


    public static MessageResponse from(Message message) {
       return new MessageResponse(
               message.getId(),
               message.getAuthorId(),
               message.getChannelId(),
               message.getContent()
       );
    }

}
