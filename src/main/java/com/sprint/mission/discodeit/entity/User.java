package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class User{
    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String userName;
    private String email;
    private String pw;

    public User(String userName, String pw, String email) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = createdAt;

        this.userName = userName;
        this.pw = pw;
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

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPw() {
        return pw;
    }

    public void update(String userName, String email, String pw) {
        this.userName = userName;
        this.email = email;
        this.pw = pw;
        this.updatedAt = System.currentTimeMillis();
    }
}
