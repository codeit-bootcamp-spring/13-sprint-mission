package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private String userName;
    private String email;
    private String password;

    private UUID profileId;

    public User(String userName, String email, String password) {
        this(userName, email, password, null);
    }

    public User(String userName, String email, String password, UUID profileId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;

        this.userName = userName;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void update(String userName, String email, String password, UUID profileId) {
        this.updatedAt = Instant.now();

        this.userName = userName;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }
}
