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

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String fileUrl;

    public BinaryContent(String fileName, Long size, String contentType, String fileUrl) {

        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.fileUrl= fileUrl;

    }


}
