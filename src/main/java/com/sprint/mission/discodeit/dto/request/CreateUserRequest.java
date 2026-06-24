package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateUserRequest  {

    private String username;
    private String email;
    private String password;

    public User toEntity(String encodedPassword) {
        return new User(
                username,
                email,
                encodedPassword
        );
    }
}
