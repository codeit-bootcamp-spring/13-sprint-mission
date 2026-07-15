package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity // 이 클래스는 JPA가 관리, 이 클래스는 데이터베이스의 한 행(인스턴스)에 정확하게 대응
@Table(name = "binary_contents") // 엔티티가 매핑될 테이블 지정 - 생략하면 클래스 이름이 테이블 이름으로 사용
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 protected로 선언(JPA가 사용하는 생성자)
public class BinaryContent extends BaseEntity {

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false, length = 100)
  private String contentType; // 같은 타입이라도 한 줄 작성 보다 각각 따로 작성하자

  @Column(nullable = false)
  private Long size; // 제공된 API 스펙에 맞추어 변경

  public BinaryContent(String fileName, String contentType, Long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
  }
}
