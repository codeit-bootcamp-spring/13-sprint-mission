package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 50
    )
    private String username;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;

    @Column(
            name = "password",
            nullable = false,
            length = 60
    )
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private BinaryContent profile;

    public User(UserData data) {
        applyUserData(data);
        this.role = Role.USER;
        this.profile = null;
    }

    public void update(UserData data) {
        applyUserData(data);
        markUpdated();
    }

    public void update(
            UserData data,
            BinaryContent profile
    ) {
        applyUserData(data);
        this.profile = profile;
        markUpdated();
    }

    public void updateProfile(BinaryContent profile) {
        this.profile = profile;
        markUpdated();
    }

    public void updateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "사용자 권한은 필수입니다."
            );
        }

        this.role = role;
        markUpdated();
    }

    public UUID getProfileId() {
        return profile == null
                ? null
                : profile.getId();
    }

    private void applyUserData(UserData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "사용자 정보는 필수입니다."
            );
        }

        this.username = data.username();
        this.email = data.email();
        this.password = data.password();
    }
}