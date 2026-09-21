package com.sprint.mission.discodeit.controller.swagger;

import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

    @Operation(summary = "csrf-token 발급")
    ResponseEntity<Void> getCsrfToken(@Parameter(hidden = true) CsrfToken csrfToken);

    @Operation(summary = "세션을 통한 유저 반환")
    ResponseEntity<UserDto> getMe(@Parameter(hidden = true) UserDto userDto);

    @Operation(
            summary = "리프레시 토큰으로 사용자 정보와 액세스 토큰 갱신",
            description = "액세스 토큰이 없거나 만료된 상태에서도 호출할 수 있습니다. "
                    + "REFRESH_TOKEN 쿠키와 유효한 CSRF 쿠키·헤더가 필요합니다. "
                    + "새 리프레시 토큰은 응답 본문이 아닌 Set-Cookie로 전달합니다.",
            parameters = {
                    @Parameter(name = "REFRESH_TOKEN", in = ParameterIn.COOKIE, required = true,
                            description = "로그인 또는 갱신 시 발급받은 리프레시 토큰",
                            schema = @Schema(type = "string")),
                    @Parameter(name = "XSRF-TOKEN", in = ParameterIn.COOKIE, required = true,
                            description = "CSRF 토큰 발급 API에서 받은 쿠키",
                            schema = @Schema(type = "string")),
                    @Parameter(name = "X-XSRF-TOKEN", in = ParameterIn.HEADER, required = true,
                            description = "XSRF-TOKEN 쿠키와 동일한 CSRF 토큰 값",
                            schema = @Schema(type = "string"))
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtDto.class)),
                    headers = @Header(name = "Set-Cookie", description = "새 REFRESH_TOKEN 쿠키",
                            schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 누락·만료·변조 등으로 갱신 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "CSRF 검증 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<JwtDto> refreshToken();

    @Operation(summary = "사용자 권한 수정")
    ResponseEntity<UserDto> updateRole(UserRoleUpdateRequest request);
}
