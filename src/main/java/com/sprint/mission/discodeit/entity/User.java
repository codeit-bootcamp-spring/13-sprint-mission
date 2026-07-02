package com.sprint.mission.discodeit.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class User extends BaseUpdatableEntity {
    private String email;
    private String password;
    private String name;
    private UUID profileId;
}
