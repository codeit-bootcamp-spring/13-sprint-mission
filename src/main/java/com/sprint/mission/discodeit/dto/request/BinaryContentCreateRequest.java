package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class BinaryContentCreateRequest {
    private UUID userId;
    private UUID messageId;
    private String filename;
    private String contentType;
    private byte[] bytes;
}

