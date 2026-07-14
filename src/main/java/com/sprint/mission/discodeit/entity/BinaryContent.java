package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Lob
    @Column(name = "bytes", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    public BinaryContent(String fileName, Long size, String contentType, byte[] data) {

        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.data = data;

    }


}
