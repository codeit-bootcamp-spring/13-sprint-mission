package com.sprint.mission.discodeit.entity;

//너무 내부 로직에 치우친 공부-> 메서드 호출 방향, 받은 값의 출처, 반환 값받을 누가 받는지 등을 더 참고.

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "users")
@Getter
public class User extends BaseUpdatableEntity {

  private String userName;
  private String password;
  private String email;

  @OneToOne
  @JoinColumn(name = "profile_id")
  private BinaryContent profile;

  @OneToOne(mappedBy = "user")//FK주인은 UserStatus의 user필드
  private UserStatus userStatus;

  public User() {
  }

  public User(String username, String password, String email) {//유저가 입력한 문자열을 받아서 {}를 실행
    this.userName = username;//(String username)로 받은 정보를 할당
    this.password = password;
    this.email = email;
  }

  public void updateProfileId(BinaryContent profileId) {
    this.profile = profileId;
  }

  public void update(String newUsername, String newEmail, String newPassword) {

    if (newUsername != null && !newUsername.equals(this.userName)) {
      this.userName = newUsername;

    }
    if (newEmail != null && !newEmail.equals(this.email)) {
      this.email = newEmail;

    }
    if (newPassword != null && !newPassword.equals(this.password)) {
      this.password = newPassword;
    }
  }
}