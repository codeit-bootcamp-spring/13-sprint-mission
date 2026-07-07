package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.util.*;

@Getter
public class User extends BaseEntity implements Serializable {
    final static long serialVersionUID = 1L;
    private String userName;
    private String email;
    private String password;
    private UUID profileId;

    public User(String userName, String email, String password, UUID profileId) {
        super();
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.profileId = profileId;
    }

    public void updateProfileId(UUID profileId) {
        this.profileId = profileId;
        setUpdatedAt();
    }

    public void updateUserName(String userName) {
        this.userName = userName;
        setUpdatedAt();
    }

    public void updateEmail(String email) {
        this.email = email;
        setUpdatedAt();
    }

    public void updatePassWord(String password) {
        this.password = password;
        setUpdatedAt();
    }

}




