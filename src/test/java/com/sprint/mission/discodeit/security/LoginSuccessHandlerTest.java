package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.handler.LoginSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginSuccessHandler 단위 테스트")
class LoginSuccessHandlerTest {

    @Mock
    Authentication authentication;

    ObjectMapper objectMapper;
    LoginSuccessHandler loginSuccessHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        loginSuccessHandler = new LoginSuccessHandler(objectMapper);
    }

    @Test
    @DisplayName("인증에 성공하면 200 응답과 사용자 DTO를 JSON으로 반환한다")
    void onAuthenticationSuccess_returnsUserDto() throws Exception {
        UserDto userDto = userDto();
        DiscodeitUserDetails principal = new DiscodeitUserDetails(userDto, "$2a$10$encodedPassword");
        given(authentication.getPrincipal()).willReturn(principal);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        JsonNode body = objectMapper.readTree(response.getContentAsString(StandardCharsets.UTF_8));
        UserDto responseBody = objectMapper.treeToValue(body, UserDto.class);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(responseBody)
                .usingRecursiveComparison()
                .ignoringFields("createdAt", "updatedAt")
                .isEqualTo(userDto);
        assertThat(responseBody.createdAt().toInstant()).isEqualTo(userDto.createdAt().toInstant());
        assertThat(responseBody.updatedAt().toInstant()).isEqualTo(userDto.updatedAt().toInstant());
    }

    private UserDto userDto() {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-10T10:00:00+09:00");
        return new UserDto(
                UUID.randomUUID(),
                "testUser",
                "test@example.com",
                null,
                true,
                Role.USER,
                now,
                now
        );
    }
}
