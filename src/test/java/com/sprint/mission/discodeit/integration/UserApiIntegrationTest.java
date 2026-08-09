package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("사용자를 생성하면 DB에 저장되고 201을 반환한다")
    void createUser_success() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", "hong12@test.com")
                                .param("password", "12345")
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("홍길동")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("hong12@test.com")
                );

        Optional<User> savedUser =
                userRepository.findByEmail("hong12@test.com");

        assertThat(savedUser)
                .isPresent();

        assertThat(savedUser.get().getUsername())
                .isEqualTo("홍길동");

        assertThat(savedUser.get().getEmail())
                .isEqualTo("hong12@test.com");
    }

    @Test
    @DisplayName("사용자를 생성한 후 사용자 목록에서 조회할 수 있다")
    void findAll_success() throws Exception {
        // given
        createUserThroughApi(
                "김철수",
                "kim2@test.com",
                "12345"
        );

        // when & then
        mockMvc.perform(
                        get("/api/users")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$")
                                .isArray()
                )
                .andExpect(
                        jsonPath(
                                "$[?(@.email == 'kim2@test.com')].username"
                        )
                                .value(hasItem("김철수"))
                );

        Optional<User> savedUser =
                userRepository.findByEmail("kim2@test.com");

        assertThat(savedUser)
                .isPresent();

        assertThat(savedUser.get().getUsername())
                .isEqualTo("김철수");
    }

    @Test
    @DisplayName("사용자 이름을 수정하면 응답과 DB에 반영된다")
    void updateUser_success() throws Exception {
        // given
        UUID userId = createUserThroughApi(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        // when & then
        mockMvc.perform(
                        multipart("/api/users/{userId}", userId)
                                .with(request -> {
                                    request.setMethod("PATCH");
                                    return request;
                                })
                                .param("username", "홍감자")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("홍감자")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("hong12@test.com")
                );

        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findById(userId)
                .orElseThrow();

        assertThat(updatedUser.getUsername())
                .isEqualTo("홍감자");

        assertThat(updatedUser.getEmail())
                .isEqualTo("hong12@test.com");
    }

    @Test
    @DisplayName("사용자를 삭제하면 204를 반환하고 DB에서 제거된다")
    void deleteUser_success() throws Exception {
        // given
        UUID userId = createUserThroughApi(
                "병아리",
                "delete@test.com",
                "12345"
        );

        // when & then
        mockMvc.perform(
                        delete("/api/users/{userId}", userId)
                )
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findById(userId))
                .isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 삭제하면 404를 반환한다")
    void deleteUser_fail_userNotFound() throws Exception {
        // given
        UUID unknownUserId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete(
                                "/api/users/{userId}",
                                unknownUserId
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("USER_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.details.userId")
                                .value(unknownUserId.toString())
                );
    }

    @Test
    @DisplayName("사용자 이름이 공백이면 생성되지 않고 400을 반환한다")
    void createUser_fail_usernameBlank() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", " ")
                                .param("email", "hong12@test.com")
                                .param("password", "12345")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.username")
                                .value("사용자 이름은 필수입니다.")
                );

        assertThat(
                userRepository.findByEmail("hong12@test.com")
        ).isEmpty();
    }

    @Test
    @DisplayName("이메일이 공백이면 생성되지 않고 400을 반환한다")
    void createUser_fail_emailBlank() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", " ")
                                .param("password", "12345")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.email")
                                .exists()
                );

        assertThat(userRepository.count())
                .isZero();
    }

    @Test
    @DisplayName("비밀번호가 공백이면 생성되지 않고 400을 반환한다")
    void createUser_fail_passwordBlank() throws Exception {
        // when & then
        mockMvc.perform(
                        multipart("/api/users")
                                .param("username", "홍길동")
                                .param("email", "hong12@test.com")
                                .param("password", " ")
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.password")
                                .exists()
                );

        assertThat(
                userRepository.findByEmail("hong12@test.com")
        ).isEmpty();
    }

    private UUID createUserThroughApi(
            String username,
            String email,
            String password
    ) throws Exception {
        MvcResult result = mockMvc.perform(
                        multipart("/api/users")
                                .param("username", username)
                                .param("email", email)
                                .param("password", password)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody =
                result.getResponse().getContentAsString();

        JsonNode responseJson =
                objectMapper.readTree(responseBody);

        return UUID.fromString(
                responseJson.get("id").asText()
        );
    }
}