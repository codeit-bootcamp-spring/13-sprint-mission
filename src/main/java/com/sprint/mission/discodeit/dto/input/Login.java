package com.sprint.mission.discodeit.dto.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Login {
    private final String email;
    private final String password;
}
