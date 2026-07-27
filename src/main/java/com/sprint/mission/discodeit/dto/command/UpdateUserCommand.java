package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;
import org.springframework.web.multipart.*;

public record UpdateUserCommand(
        String username,
        String email,
        String password,
        MultipartFile profileImage
) {

    public static UpdateUserCommand from(UpdateUserRequest request) {
        return new UpdateUserCommand(request.username(), request.email(), request.password(), request.profileImage());
    }
}
