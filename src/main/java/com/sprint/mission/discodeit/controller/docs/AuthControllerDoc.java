package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.input.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface AuthControllerDoc {

    // auth api
    @Operation(summary = "로그인 요청",
            description = "사용자 이메일과 비밀번호로 로그인을 요청한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 id의 유저가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "비밀번호가 일치하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    ResponseEntity<User> login(
            @RequestBody LoginRequest loginRequest
    );

}
