package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.exception.NoChangesException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // 도메인 모델의 getter 메소드를 @Getter로 대체
@Entity
@Table(name = "channels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseUpdatableEntity {

  @Enumerated(EnumType.STRING) // enum을 DB에 문자열로 저장, enum 순서 바꿀 때 데이터가 어긋나서 위험하기 때문에 STRING 권장
  @Column(nullable = false, length = 10)
  private ChannelType type;

  @Column(length = 100)
  private String name;

  @Column(length = 500)
  private String description; // 채널 설명 추가

  // 객체 생성 시 클래스 외부에서 정의해야 하는 값만 파라미터로 정의
  public Channel(ChannelType type, String name, String description) {
    this.type = type;
    this.name = name;
    this.description = description;
  }

  // 필드 수정하는 update 함수 정의
  public void update(String newName, String newDescription) {
    boolean anyValueUpdated = false; // updatedat은 실제 변경이 있을 때만 갱신되도록 구성
    if (newName != null && !newName.equals(this.name)) {
      this.name = newName;
      anyValueUpdated = true;
    }
    if (newDescription != null && !newDescription.equals(this.description)) { // null 체크 필요
      this.description = newDescription;
      anyValueUpdated = true;
    }
    if (!anyValueUpdated) {
      throw new NoChangesException();
    }
  }
}
