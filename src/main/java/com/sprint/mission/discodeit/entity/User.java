package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.*;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

    @Column(unique = true, nullable = false)
    private  String username;
    @Column(unique = true, nullable = false)
    private  String email;

    @Column(nullable = false)
    private  String password;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id", unique = true, nullable = true)
    private BinaryContent profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public User(String username, String email, String password) {
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = Role.USER;
    }

    public void updateProfile(BinaryContent profile) {
        this.profile = profile;
    }

    public void updateUserName(String username) {
        this.username = username;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateRole(Role role) {
        if( role == null) {
            throw new IllegalArgumentException("권한은 필수 입니다.");
        }
        this.role = role;
    }

}




