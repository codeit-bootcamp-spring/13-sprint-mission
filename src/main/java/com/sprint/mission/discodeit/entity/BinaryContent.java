package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.time.*;
import java.util.*;
@Getter
public class BinaryContent implements Serializable {

    private final UUID id;
    private final Instant createdAt;

    private final String contentType;
    private final byte[] data;
    private final String fileName;
    private final Long size;

    public BinaryContent(String fileName, Long size, String contentType, byte[] data) {

        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();

        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.data = data;

    }


}
