package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
public class User extends BaseUpdatableEntity {
    private String username;
    private String email;
    private String password;
    private BinaryContent profile;
    private UserStatus status;

    public User(
            UUID id,
            Instant ctime,
            Instant mtime,
            String username,
            String email,
            String password,
            BinaryContent profile,
            UserStatus status
            ){
        super(id, ctime, mtime);
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
        this.status = status;
    }

    public User(
            String username,
            String email,
            String password,
            BinaryContent profile,
            UserStatus status
    ){
        super();
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
        this.status = status;
    }


}
