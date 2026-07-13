package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 60, nullable = false)
    private String password;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "profile_id", unique = true)
    private BinaryContent profile;

    @OneToOne(mappedBy = "user", fetch = EAGER, cascade = ALL, orphanRemoval = true)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = ALL, orphanRemoval = true)
    private final List<ReadStatus> readStatuses = new ArrayList<>();

    private User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public static User create(String username, String email, String password) {
        return new User(username, email, password);
    }

    public void update(String newUsername, String newEmail, String newPassword) {
        if (newUsername != null) {
            this.username = newUsername;
        }
        if (newEmail != null) {
            this.email = newEmail;
        }
        if (newPassword != null) {
            this.password = newPassword;
        }
    }

    public void updateProfile(BinaryContent newProfile) {
        if (newProfile != null) {
            this.profile = newProfile;
        }
    }

    public void updateStatus(UserStatus newStatus) {
        this.status = newStatus;
    }
}