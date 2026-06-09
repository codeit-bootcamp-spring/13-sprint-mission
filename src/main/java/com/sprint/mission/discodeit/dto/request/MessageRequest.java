package com.sprint.mission.discodeit.dto.request;

import org.springframework.web.multipart.*;

import java.time.*;
import java.util.*;

public record MessageRequest(
        UUID authorId,
        UUID channelId,
        String content,
        List<MultipartFile> attachments
) {

    public record AttachmentRequest(
            String fileName,
            String contentType,
            byte[] data
    ) {
    }

    public record CreateMessageRequest(
            UUID authorId,
            UUID channelId,
            String content,
            List<AttachmentRequest> attachments
    ) {
    }
}
