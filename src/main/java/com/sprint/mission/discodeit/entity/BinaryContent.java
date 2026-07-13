package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class
BinaryContent extends BaseEntity {

    @Column(length = 255, nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    @Column(length = 100, nullable = false)
    private String contentType;

    private BinaryContent(String fileName, Long size, String contentType) {
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }

    public static BinaryContent create(String fileName, Long size, String contentType) {
        return new BinaryContent(fileName, size, contentType);
    }
}