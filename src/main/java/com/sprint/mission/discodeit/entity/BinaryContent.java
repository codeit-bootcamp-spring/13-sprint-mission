package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class BinaryContent extends BaseEntity{
    private final String fileName;
    private final String contentType;
    private final Long size;
    private final byte[] bytes;

    // restore constructor
    public BinaryContent(
            UUID id,
            Instant ctime,
            String fileName,
            String contentType,
            Long size,
            byte[] bytes
    ) {
        super(id, ctime);
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.bytes = bytes;
    }

    // generation constructor
    public BinaryContent(
            String fileName,
            String contentType,
            Long size,
            byte[] bytes
    ) {
        super();
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.bytes = bytes;
    }
}
