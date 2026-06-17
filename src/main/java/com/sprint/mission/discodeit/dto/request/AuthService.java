package com.sprint.mission.discodeit.dto.request;

public record AuthService () {

    public record login(
            String username,
            String password
    ) {}
}
