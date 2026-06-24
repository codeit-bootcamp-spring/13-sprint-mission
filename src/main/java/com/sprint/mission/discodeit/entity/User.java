package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
@Getter
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private String username;
    private String email;
    private String password;  // 직렬화에서 제외(transient) 저장 안되어 로그인 불가로 제거

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public boolean checkPassword(String rawPassword) {
        return this.password.equals(rawPassword);
    }

    public void update(String username, String email, String encodedPassword) {
        if (username != null && !username.isBlank()) {
            this.username = username;
        }
        if (email != null) {
            this.email = email;
        }
        if (encodedPassword != null) {
            this.password = encodedPassword;
        }

        this.updatedAt = Instant.now();
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
