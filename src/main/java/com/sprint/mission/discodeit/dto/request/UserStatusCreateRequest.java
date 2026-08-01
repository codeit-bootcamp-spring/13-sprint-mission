package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserStatusCreateRequest(
    @NotNull(message = "userId를 입력해주세요.")
    UUID userId) {

}