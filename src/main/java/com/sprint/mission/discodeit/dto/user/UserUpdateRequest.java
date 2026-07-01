package com.sprint.mission.discodeit.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;


public record UserUpdateRequest(
        @JsonProperty("newUsername")
        String username,

        @JsonProperty("newEmail")
        String email,

        @JsonProperty("newPassword")
        String password,

        String profileName,
        String profileContentType,
        byte[] profileBytes
) {
}
