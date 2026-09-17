package com.sprint.mission.discodeit.controller.swagger;

import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary = "csrf-token 발급")
    ResponseEntity<Void> getCsrfToken(@Parameter(hidden = true) CsrfToken csrfToken);

    @Operation(summary = "세션을 통한 유저 반환")
    ResponseEntity<UserDto> getMe(@Parameter(hidden = true) UserDto userDto);

    @Operation(summary = "사용자 권한 수정")
    ResponseEntity<UserDto> updateRole(UserRoleUpdateRequest request);
}
