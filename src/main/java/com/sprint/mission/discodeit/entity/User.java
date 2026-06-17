package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final Instant createdAt;

    private Instant updatedAt;

    private String username;

    private String email;

    private String password;

    private UUID profileId;

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void update(String newUsername, String newEmail, String newPassword) {
        boolean flag = false;

        if (newUsername != null && !newUsername.equals(this.username)) {
            this.username = newUsername;
            flag = true;
        }
        if (newEmail != null && !newEmail.equals(this.email)) {
            this.email = newEmail;
            flag = true;
        }
        if (newPassword != null && !newPassword.equals(this.password)) {
            this.password = newPassword;
            flag = true;
        }

        if (flag) {
            this.updatedAt = Instant.now();
        }
    }

    public void updateProfileId(UUID newProfileId) {
        if (newProfileId != null && !newProfileId.equals(this.profileId)) {
            this.profileId = newProfileId;
            this.updatedAt = Instant.now();
        }
    }
}
