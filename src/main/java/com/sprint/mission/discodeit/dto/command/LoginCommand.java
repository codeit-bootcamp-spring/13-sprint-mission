package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;

public record LoginCommand(
        String email,
        String password
) {
    public LoginCommand from(LoginRequest request) {
            return new LoginCommand(request.email(), request.password());
    }
}
