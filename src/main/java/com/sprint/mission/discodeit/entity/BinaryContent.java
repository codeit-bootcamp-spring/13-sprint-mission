package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  // 클래스 버전 식별자, 역직렬화할 때 값을 비교하기 위해 사용한다
  // 명시하지 않는다면 자바가 이를 자동 계산하는데, 컴파일러·JVM 버전에 따라 다를 수 있다 -> 호환성이 보장되지 않는 문제
  // 따라서 호환성을 보장하기 위해 serialVersionUID = 1L을 선언해주어야 한다

  private final UUID id;
  private final Instant createdAt;
  private String fileName;
  private String contentType; // 같은 타입이라도 한 줄 작성 보다 각각 따로 작성하자
  private Long size; // 제공된 API 스펙에 맞추어 변경
  private byte[] bytes; // 실제 파일 데이터

  public BinaryContent(String fileName, String contentType, Long size, byte[] bytes) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
    this.bytes = bytes;
  }
}
