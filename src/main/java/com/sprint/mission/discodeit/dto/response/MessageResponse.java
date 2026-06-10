package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public record MessageResponse(
        UUID id,
        UUID authorId,
        UUID channelId,
        String content,
        List<AttachmentResponse> attachments
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


    public static MessageResponse from(Message message,List<BinaryContent> attachments) {
       return new MessageResponse(
               message.getId(),
               message.getAuthorId(),
               message.getChannelId(),
               message.getContent(),
               attachments.stream().map(AttachmentResponse::from).toList()
       );
    }

    @Override
    public String toString() {
        return """
            Message
            ====================
            ID          : %s
            Channel ID  : %s
            Author ID   : %s
            Content     : %s
            Attachments : %s
            ====================
            """
                .formatted(
                        id,
                        channelId,
                        authorId,
                        content,
                        attachments == null ? "[]" : attachments
                );
    }
}
