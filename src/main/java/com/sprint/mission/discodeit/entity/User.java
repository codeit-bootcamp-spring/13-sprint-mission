package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private String email;
    private String password;
    private String username;
    private UserStatus status;

    public enum UserStatus {
        ONLINE, OFFLINE, AWAY, DO_NOT_DISTURB
    }

    public User(String username, String email, String password) {

        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.email = email;
        this.password = password;
        this.username = username;
        this.status = UserStatus.OFFLINE;
    }


    public void updateProfile(String username) {
        this.username = username;
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void updatePassword(String password) {
        this.password = password;
        this.updatedAt = Instant.now();
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", status=" + status +
                '}';
    }

}
