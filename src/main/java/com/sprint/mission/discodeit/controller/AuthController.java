package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    // CSRF 토큰 발급
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(
            CsrfToken csrfToken
    ) {

        String tokenValue =
                csrfToken.getToken();

        log.debug(
                "CSRF 토큰 요청: {}",
                tokenValue
        );

        return ResponseEntity
                .status(
                        HttpStatus.NON_AUTHORITATIVE_INFORMATION
                )
                .build();
    }

    // 현재 로그인한 사용자 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal
            DiscodeitUserDetails userDetails
    ) {

        UserResponse response =
                userService.read(
                        userDetails
                                .getUserDto()
                                .getId()
                );

        return ResponseEntity.ok(
                response
        );
    }

    // 사용자 권한 수정
    @PutMapping("/role")
    public ResponseEntity<UserResponse> updateRole(
            @Valid
            @RequestBody
            UserRoleUpdateRequest request
    ) {

        UserResponse response =
                userService.updateRole(
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }
}