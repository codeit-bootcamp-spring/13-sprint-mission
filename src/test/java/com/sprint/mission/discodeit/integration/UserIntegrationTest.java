package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 생성 API 요청이 성공하면 데이터베이스에 저장된다")
    void createUserSuccess() throws Exception {
        // given
        String requestJson = """
                {
                  "username": "integration-user",
                  "email": "integration-user@example.com",
                  "password": "password123"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username")
                        .value("integration-user"))
                .andExpect(jsonPath("$.email")
                        .value("integration-user@example.com"))
                .andExpect(jsonPath("$.online").value(false));

        assertThat(
                userRepository.existsByUsername("integration-user")
        ).isTrue();

        assertThat(
                userRepository.existsByEmail(
                        "integration-user@example.com"
                )
        ).isTrue();
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 사용자 생성을 요청하면 400 응답을 반환한다")
    void createUserFailWhenEmailIsInvalid() throws Exception {
        // given
        String requestJson = """
                {
                  "username": "invalid-user",
                  "email": "invalid-email",
                  "password": "password123"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.details.email")
                        .value("올바른 이메일 형식이어야 합니다."));

        assertThat(
                userRepository.existsByUsername("invalid-user")
        ).isFalse();
    }

    @Test
    @DisplayName("중복된 사용자 이름으로 생성하면 409 응답을 반환한다")
    void createUserFailWhenUsernameAlreadyExists() throws Exception {
        // given
        userRepository.saveAndFlush(
                createUser(
                        "duplicate-user",
                        "original@example.com"
                )
        );

        String requestJson = """
                {
                  "username": "duplicate-user",
                  "email": "new@example.com",
                  "password": "password123"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("UserAlreadyExistsException"))
                .andExpect(jsonPath("$.details.field")
                        .value("username"))
                .andExpect(jsonPath("$.details.value")
                        .value("duplicate-user"));

        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("사용자 수정 API 요청이 성공하면 데이터베이스의 정보가 변경된다")
    void updateUserSuccess() throws Exception {
        // given
        User savedUser = userRepository.saveAndFlush(
                createUser(
                        "before-user",
                        "before@example.com"
                )
        );

        String requestJson = """
                {
                  "username": "updated-user",
                  "email": "updated@example.com",
                  "password": "updatedPassword123"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/users/{id}", savedUser.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.username")
                        .value("updated-user"))
                .andExpect(jsonPath("$.email")
                        .value("updated@example.com"));

        userRepository.flush();

        User updatedUser = userRepository
                .findById(savedUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getUsername())
                .isEqualTo("updated-user");

        assertThat(updatedUser.getEmail())
                .isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정하면 404 응답을 반환한다")
    void updateUserFailWhenUserNotFound() throws Exception {
        // given
        UUID unknownUserId = UUID.randomUUID();

        String requestJson = """
                {
                  "username": "updated-user",
                  "email": "updated@example.com",
                  "password": "updatedPassword123"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/users/{id}", unknownUserId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("UserNotFoundException"))
                .andExpect(jsonPath("$.details.userId")
                        .value(unknownUserId.toString()));
    }

    @Test
    @DisplayName("사용자 삭제 API 요청이 성공하면 데이터베이스에서 제거된다")
    void deleteUserSuccess() throws Exception {
        // given
        User savedUser = userRepository.saveAndFlush(
                createUser(
                        "delete-user",
                        "delete-user@example.com"
                )
        );

        UUID userId = savedUser.getId();

        // when & then
        mockMvc.perform(
                        delete("/users/{id}", userId)
                )
                .andExpect(status().isOk());

        userRepository.flush();

        assertThat(userRepository.findById(userId))
                .isEmpty();

        mockMvc.perform(
                        get("/users/{id}", userId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 목록 조회 API는 저장된 사용자 목록을 반환한다")
    void findAllUsersSuccess() throws Exception {
        // given
        userRepository.save(
                createUser(
                        "user01",
                        "user01@example.com"
                )
        );

        userRepository.save(
                createUser(
                        "user02",
                        "user02@example.com"
                )
        );

        userRepository.flush();

        // when & then
        mockMvc.perform(
                        get("/users")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].username")
                        .value(hasItem("user01")))
                .andExpect(jsonPath("$[*].username")
                        .value(hasItem("user02")));
    }

    private User createUser(
            String username,
            String email
    ) {
        return new User(
                username,
                email,
                "password123",
                null
        );
    }
}