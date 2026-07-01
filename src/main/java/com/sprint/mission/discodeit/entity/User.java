package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class User extends BaseUpdatableEntity {

    //필드
    private String username;
    private String email;
    private String password;
    private UUID profileId;

    //ctor
    public User(String username, String email, String password, UUID profileId) {
        super();

        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    //update Method
    public void updateUser(String username, String email, String password, UUID profileId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileId = profileId;

        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "[User: " + username + ", Email: " + email + "]";
    }
}
