package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginSuccessHandler 테스트")
class LoginSuccessHandlerTest {

    @Mock
    private Authentication authentication;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Test
    @DisplayName("로그인에 성공하면 200과 사용자 정보를 JSON으로 반환")
    void login_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto userDto = new UserDto(
                userId,
                "user1",
                "user1@test.com",
                null,
                true,
                Role.USER
        );

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(
                        userDto,
                        "encoded-password"
                );

        given(authentication.getPrincipal())
                .willReturn(userDetails);

        LoginSuccessHandler handler =
                new LoginSuccessHandler(objectMapper);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        // when
        handler.onAuthenticationSuccess(
                request,
                response,
                authentication
        );

        // then
        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.OK.value());

        assertThat(response.getContentType())
                .startsWith(MediaType.APPLICATION_JSON_VALUE);

        assertThat(response.getCharacterEncoding())
                .isEqualTo("UTF-8");

        JsonNode responseBody =
                objectMapper.readTree(
                        response.getContentAsString()
                );

        assertThat(responseBody.get("id").asText())
                .isEqualTo(userId.toString());

        assertThat(responseBody.get("username").asText())
                .isEqualTo("user1");

        assertThat(responseBody.get("email").asText())
                .isEqualTo("user1@test.com");

        assertThat(responseBody.get("online").asBoolean())
                .isTrue();

        assertThat(responseBody.get("role").asText())
                .isEqualTo("USER");

        then(authentication).should()
                .getPrincipal();
    }
}