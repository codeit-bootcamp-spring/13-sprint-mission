package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString(exclude = "password")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final Instant createdAt;
    private Instant updatedAt;
    private String name;
    private String email;
    private String password;
    private UUID profileId;


    public User (String name, String email, String password) {
        this.userId = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public void updateUserName(String name){
        this.name = name;
        this.updatedAt = Instant.now();
    }
    public void updateUserEmail(String email){
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updateUserPassword(String password){
        this.password = password;
        this.updatedAt = Instant.now();
    }
}
