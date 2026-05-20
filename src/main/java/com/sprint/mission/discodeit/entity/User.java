package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class User {

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String email;
    private String password;
    private String username;
    private UserStatus status;

    public enum UserStatus {
        ONLINE, OFFLINE, AWAY, DO_NOT_DISTURB
    }

    public User(String username, String email, String password) {

        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.email = email;
        this.password = password;
        this.username = username;
        this.status = UserStatus.OFFLINE;
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
    public String getEmail() {
        return email;
    }
    public String getUsername() {
        return username;
    }
    public UserStatus getStatus() {
        return status;
    }

    public void updateProfile(String username) {
        this.username = username;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updatePassword(String password) {
        this.password = password;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
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
