package com.sprint.mission.discodeit.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record UserUpdateRequest(
        @JsonProperty("newUsername")
        @Size(max = 50)
        String username,

        @JsonProperty("newEmail")
        @Size(max = 100)
        @Email
        String email,

        @JsonProperty("newPassword")
        @Size(min = 8, max = 60)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$")
        String password,

        String profileName,
        String profileContentType,
        byte[] profileBytes
) {
}
