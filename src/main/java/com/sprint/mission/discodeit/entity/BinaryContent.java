package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "binary_contents")
@Getter
public class BinaryContent extends BaseEntity {

  private String fileName;
  private long size;
  private String contentType;

  protected BinaryContent() {
  }

  public BinaryContent(String fileName, long size,
      String contentType) {

    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;

  }

}
