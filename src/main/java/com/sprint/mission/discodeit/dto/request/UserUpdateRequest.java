package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record UserUpdateRequest (
    UUID id,
    String username,
    String email,
    String password,
    UUID profileId
){
    public static UserUpdateRequest from(
            UUID id,
            String username,
            String email, String password, UUID profileId) {
        return new UserUpdateRequest(id, username, email, password, profileId);
    }

}