package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;
import org.hibernate.sql.*;

import java.time.*;

public record UpdateReadStatusCommand(
        Instant lastReadTime
) {

    public UpdateReadStatusCommand from(UpdateReadStatusRequest request) {
        return new UpdateReadStatusCommand(request.lastReadTime());
    }
}
