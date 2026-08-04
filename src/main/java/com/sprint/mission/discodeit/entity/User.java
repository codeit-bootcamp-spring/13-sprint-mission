package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
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

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private BinaryContent profile;

    public User(String username, String email, String password) {
        validate(username, email, password);

        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = null;
    }

    public void update(String username, String email, String password) {
        validate(username, email, password);

        this.username = username;
        this.email = email;
        this.password = password;
        markUpdated();
    }

    public void update(String username, String email, String password, BinaryContent profile) {
        validate(username, email, password);

        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
        markUpdated();
    }

    public void updateProfile(BinaryContent profile) {
        this.profile = profile;
        markUpdated();
    }

    public UUID getProfileId() {
        if (profile == null) {
            return null;
        }

        return profile.getId();
    }

    private void validate(String username, String email, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("사용자 이름은 비어 있을 수 없습니다.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 비어 있을 수 없습니다.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }
    }
}