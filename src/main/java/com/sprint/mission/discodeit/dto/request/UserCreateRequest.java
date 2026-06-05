package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record UserCreateRequest (
    String username,
    String email,
    String password,
    UUID profileId
){
    public static UserCreateRequest from(
            String username, String email, String password, UUID profileId) {
        return new UserCreateRequest(username, email, password, profileId);
    }

}