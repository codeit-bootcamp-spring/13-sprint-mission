package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;


@Getter
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

    @Column(nullable = false ,length = 255)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false, length = 100)
    private String contentType;

    protected BinaryContent() {}

    public BinaryContent(String fileName, Long size, String contentType) {
        super();
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
    }
}