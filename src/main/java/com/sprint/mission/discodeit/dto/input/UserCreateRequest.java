package com.sprint.mission.discodeit.dto.input;

public record UserCreateRequest (
    String username,
    String email,
    String password
){}
