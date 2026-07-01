package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent extends BaseEntity {

    // 필드
    private final String fileName;
    private final Long size;
    private final String contentType;
    private final byte[] bytes;

    //ctor
    public BinaryContent(String fileName, Long size, String contentType, byte[] bytes) {
        super();

        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.bytes = bytes;
    }
}
