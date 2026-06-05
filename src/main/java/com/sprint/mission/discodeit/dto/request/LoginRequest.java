package com.sprint.mission.discodeit.dto.request;

public record LoginRequest(
        String email,
        String password
) {
    public static LoginRequest from(String email, String password) {
        return new LoginRequest(email, password);
    }
}
