package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

@Getter
@ToString
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String userName;
    private String email;
    private String pw;

    public User(String userName, String email, String pw) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = createdAt;

        this.userName = userName;
        this.email = email;
        this.pw = pw;
    }

    public void update(String userName, String email, String pw) {
        this.userName = userName;
        this.email = email;
        this.pw = pw;
        this.updatedAt = System.currentTimeMillis();
    }
}
