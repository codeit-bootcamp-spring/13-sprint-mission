package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

  @Column(unique = true, nullable = false, length = 50)
  private String username;
  @Column(unique = true, nullable = false, length = 100)
  private String email;
  @Column(nullable = false, length = 60)
  private String password;

  @OneToOne
  @JoinColumn(name = "profile_id", unique = true)
  private BinaryContent profile;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private UserStatus status;

  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
  }

  public void update(String username, String email, String password, BinaryContent profile) {
    if (username != null) {
      this.username = username;
    }
    if (email != null) {
      this.email = email;
    }
    if (password != null) {
      this.password = password;
    }
    if (profile != null) {
      this.profile = profile;
    }
  }

  public void updateStatus(UserStatus status) {
    this.status = status;
  }
}