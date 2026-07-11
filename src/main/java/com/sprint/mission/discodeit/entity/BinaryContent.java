package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

    @Column(name = "file_name", nullable = false, length = 255)
   private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
   private String contentType;

    @Column(name = "size", nullable = false)
   private Long size;


    public BinaryContent(String fileName, String contentType, Long size) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
    }




}
