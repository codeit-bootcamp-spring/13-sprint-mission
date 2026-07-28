package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.exception.NoChangesException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

  @JoinColumn(name = "profile_id", columnDefinition = "uuid")
  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  // User 저장할 때 프로필도 함께 저장한다
  private BinaryContent profile;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 60)
  private String password; // 비밀번호 추가

  @JsonManagedReference // Jackson 통해 직렬화하는 경우 순환 참조 발생할 수 있기 때문에 이를 방지하기 위해 추가
  @Setter(AccessLevel.PROTECTED)
  // 양방향 참조 관계 -> UserStatus 생성자에 User에도 참조 관계 정의, 도메인 내부에서만 사용 가능하도록 PROTECTED
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  // UserStatus는 User에 완전히 종속적 - ON DELETE CASCADE
  private UserStatus status; // UserStatus 참조

  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
  }

  // updatedAt은 메소드 내부 수정이 발생했을 때만 현재 시간으로 수정하기 때문에 파라미터로 받지 않는다
  public void update(String newUserName, String newEmail, String newPassword,
      BinaryContent newProfile) {
    boolean anyValueUpdated = false; // updatedat은 실제 변경이 있을 때만 갱신되도록 구성
    if (newUserName != null && !newUserName.equals(this.username)) {
      this.username = newUserName;
      anyValueUpdated = true;
    }
    if (newEmail != null && !newEmail.equals(this.email)) { // 기존 값과 다를 때 업데이트 되도록 해야 한다
      this.email = newEmail;
      anyValueUpdated = true;
    }
    if (newPassword != null && !newPassword.equals(this.password)) {
      this.password = newPassword;
      anyValueUpdated = true;
    }
    if (!newProfile.equals(this.profile)) { // 프로필은 선택적으로 등록 가능하기 때문에 null로 변경 가능
      this.profile = newProfile;
      anyValueUpdated = true;
    }
    if (!anyValueUpdated) {
      throw new NoChangesException();
    }
  }
}
