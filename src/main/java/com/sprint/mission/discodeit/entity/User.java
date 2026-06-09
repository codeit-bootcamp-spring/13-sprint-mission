package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private String email;
    private String password;
    private String username;
//    private UserStatus status;
    private UUID profileId;

//    public enum UserStatus {
//        ONLINE, OFFLINE, AWAY, DO_NOT_DISTURB
//    }

    public User(String username, String email, String password) {

        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.email = email;
        this.password = password;
        this.username = username;
//        this.status = UserStatus.OFFLINE;
        this.profileId = profileId;
    }

    public void update(String username, String email, String password, UUID profileId) {

        if (username != null) this.username = username;
        if (email != null) this.email = email;
        if (password != null) this.password = password;
        if (profileId != null) this.profileId = profileId;

        this.updatedAt = Instant.now();


}

//    public void updateProfile(String newUsername) {
//    }
}
