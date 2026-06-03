package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    private String username;
    private String email;

    public User(String username, String email) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now().toEpochMilli();

        this.username = username;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void update(String username, String email) {
        this.username = username;
        this.email = email;
        this.updatedAt = Instant.now().toEpochMilli();
    }
}