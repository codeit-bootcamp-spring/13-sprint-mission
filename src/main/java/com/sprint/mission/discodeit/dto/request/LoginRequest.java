package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "name은 비워둘 수 없습니다.")
        String username,

        @NotBlank(message = "password는 비워둘 수 없습니다.")
        String password
) {

}
