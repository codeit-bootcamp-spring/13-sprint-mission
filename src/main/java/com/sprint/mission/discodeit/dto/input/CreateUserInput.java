package com.sprint.mission.discodeit.dto.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class CreateUserInput {
    private final String name;
    private final String email;
    private final String password;
    private final UUID thumbnail;
}
