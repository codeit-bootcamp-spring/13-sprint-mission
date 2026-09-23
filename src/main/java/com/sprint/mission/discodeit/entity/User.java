package com.sprint.mission.discodeit.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

  @Column(nullable = false, length = 60)
  private String password;

  @Column(nullable = false, length = 50, unique = true)
  private String username;

  @Column(nullable = false, length = 100, unique = true)
  private String email;

  @Column(length = 30, unique = true)
  private String phoneNumber;

  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private BinaryContent profile;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ReadStatus> readStatuses = new ArrayList<>();

  @OneToMany(mappedBy = "author")
  private List<Message> messages = new ArrayList<>();

  protected User() {
  }

  //getter setter

  public void updatePassword(String password) {
    this.password = password;
  }

  public void updateUserName(String username) {
    this.username = username;
  }

  public void updateEmail(String email) {
    this.email = email;
  }

  public void updatePhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void updateRole(Role role) {
    this.role = role;
  }

  public void updateProfile(BinaryContent profile) {
    this.profile = profile;
  }



}
