package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    private String username;
    private String email;
    private transient String password;  // 직렬화에서 제외

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getName() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getpassword() {
        return password;
    }

    public void update(String username, String email, String password) {
        if (username != null) {
            this.username = username;
        }
        if (email != null) {
            this.email = email;
        }
        if (password != null) {
            this.password = password;
        }

        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
