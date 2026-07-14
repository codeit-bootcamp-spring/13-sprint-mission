package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends BaseUpdatableEntity {

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID profileImageId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private BinaryContent profile;

    private UserStatus status;

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.email = email;
        this.password = password;
        this.profileImageId = null;
    }

    public void update(String username, String password, String email) {
        if (username == null ||username.trim().isEmpty()) {
            return;
        }
        this.username = username;
        this.password = password;
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updateProfileId(UUID profileImageId) {
        this.profileImageId = profileImageId;
        this.updatedAt = Instant.now();
    }

}