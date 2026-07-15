package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false, columnDefinition = "bytea")
  private byte[] bytes;

  // 바이너리 데이터는 디스크에 저장
  public BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }
}