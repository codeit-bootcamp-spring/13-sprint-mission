package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class User extends EntityRoot implements Serializable {

    //필드
    private String name;
    private String email;
    private String password;
    private UUID profileId;

    //ctor
    public User(String name, String email, String password, UUID profileId) {
        super();

        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    //update Method
    public void updateUser(String name, String email, String password, UUID profileId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileId = profileId;

        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "[User: " + name + ", Email: " + email + "]";
    }
}
