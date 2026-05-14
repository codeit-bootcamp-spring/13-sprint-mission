package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public class User {

    private final UUID id;
    private final long createdAt;
    private long updatedAt;
    private String name;
    private String email;
    private String password;


    public User (String name, String email, String password) {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return id;
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

    public void updateName(String name){
        this.name = name;
        this.updatedAt = System.currentTimeMillis();
    }
    public void updateEmail(String email){
        this.email = email;
        this.updatedAt = System.currentTimeMillis();
    }

    public void updatePassword(String password){
        this.password = password;
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
