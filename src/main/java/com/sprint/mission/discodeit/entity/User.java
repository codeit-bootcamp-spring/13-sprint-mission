package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final long createdAt;
    private long updatedAt;
    private String name;
    private String email;
    private String password;


    public User (String name, String email, String password) {
        this.userId = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void updateUserName(String name){
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateUserEmail(String email){
        this.email = email;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updateUserPassword(String password){
        this.password = password;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdAt = sdf.format(new Date(this.createdAt));
        String updatedAt = sdf.format(new Date(this.updatedAt));
        return "User{" +
                "id = " + userId +
                ", 생성 시간 = " + createdAt +
                ", 업데이트 시간 = " + updatedAt +
                ", 이름 = '" + name + '\'' +
                ", 이메일 = '" + email + '\'' +
                ", 비밀번호 = '" + password + '\'' +
                '}';
    }
}
