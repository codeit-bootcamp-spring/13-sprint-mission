package com.sprint.mission.discodeit.dto.request;

import io.swagger.v3.oas.annotations.media.*;

@Schema(description = "로그인 정보")
public record LoginRequest(String email,
                           String password) {

}
