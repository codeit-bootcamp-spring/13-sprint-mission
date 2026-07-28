package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("사용자 생성 API")
    class CreateUser {

        @Test
        @DisplayName("정상적인 요청이면 사용자를 생성하고 201을 반환")
        void 사용자_생성_성공() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MockMultipartFile requestPart = createUserRequestPart(request);

            // when & then
            mockMvc.perform(multipart("/api/users")
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.username")
                            .value("user1"))
                    .andExpect(jsonPath("$.email")
                            .value("user1@test.com"));

            assertThat(userRepository.findAll()).hasSize(1);
            assertThat(userRepository.findAll().get(0).getUsername())
                    .isEqualTo("user1");
            assertThat(userRepository.findAll().get(0).getEmail())
                    .isEqualTo("user1@test.com");
        }

        @Test
        @DisplayName("중복된 이메일로 생성하면 409를 반환")
        void 사용자_생성_실패_이메일_중복() throws Exception {
            // given
            createUser(
                    "existing-user",
                    "duplicate@test.com",
                    "password1"
            );

            UserCreateRequest duplicateRequest =
                    new UserCreateRequest(
                            "new-user",
                            "duplicate@test.com",
                            "password2"
                    );

            MockMultipartFile requestPart = createUserRequestPart(duplicateRequest);

            // when & then
            mockMvc.perform(multipart("/api/users")
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code")
                            .value("DUPLICATE_USER_EMAIL"));

            assertThat(userRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("사용자 수정 API")
    class UpdateUser {

        @Test
        @DisplayName("존재하는 사용자의 정보를 수정하고 200을 반환")
        void 사용자_수정_성공() throws Exception {
            // given
            UUID userId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            UserUpdateRequest request = new UserUpdateRequest(
                    "updated-user",
                    "updated@test.com",
                    "updated-password"
            );

            MockMultipartFile requestPart = createUserUpdateRequestPart(request);

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}", userId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(userId.toString()))
                    .andExpect(jsonPath("$.username")
                            .value("updated-user"))
                    .andExpect(jsonPath("$.email")
                            .value("updated@test.com"));

            User updatedUser = userRepository
                    .findById(userId)
                    .orElseThrow();

            assertThat(updatedUser.getUsername())
                    .isEqualTo("updated-user");
            assertThat(updatedUser.getEmail())
                    .isEqualTo("updated@test.com");
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 수정하면 404를 반환")
        void 사용자_수정_실패_사용자_없음() throws Exception {
            // given
            UUID unknownUserId = UUID.randomUUID();

            UserUpdateRequest request = new UserUpdateRequest(
                    "updated-user",
                    "updated@test.com",
                    "updated-password"
            );

            MockMultipartFile requestPart = createUserUpdateRequestPart(request);

            // when & then
            mockMvc.perform(multipart("/api/users/{userId}", unknownUserId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.details.userId")
                            .value(unknownUserId.toString()));

            assertThat(userRepository.findById(unknownUserId))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자 삭제 API")
    class DeleteUser {

        @Test
        @DisplayName("존재하는 사용자를 삭제하고 204를 반환")
        void 사용자_삭제_성공() throws Exception {
            // given
            UUID userId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            // when & then
            mockMvc.perform(delete("/api/users/{userId}",userId))
                    .andExpect(status().isNoContent());

            assertThat(userRepository.findById(userId))
                    .isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 404를 반환")
        void 사용자_삭제_실패_사용자_없음() throws Exception {
            // given
            UUID unknownUserId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/users/{userId}", unknownUserId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.details.userId")
                            .value(unknownUserId.toString()));

            assertThat(userRepository.findById(unknownUserId))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("사용자 목록 조회 API")
    class FindAllUsers {

        @Test
        @DisplayName("저장된 모든 사용자 목록을 반환")
        void 사용자_목록_조회_성공() throws Exception {
            // given
            createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            createUser(
                    "user2",
                    "user2@test.com",
                    "password2"
            );

            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()")
                            .value(2))
                    .andExpect(jsonPath("$[0].username")
                            .exists())
                    .andExpect(jsonPath("$[0].email")
                            .exists())
                    .andExpect(jsonPath("$[1].username")
                            .exists())
                    .andExpect(jsonPath("$[1].email")
                            .exists());

            assertThat(userRepository.findAll())
                    .hasSize(2);
        }

        @Test
        @DisplayName("사용자가 없으면 빈 목록을 반환")
        void 사용자_목록_조회_결과_없음() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            assertThat(userRepository.findAll())
                    .isEmpty();
        }
    }




    private UUID createUser(String username, String email, String password) throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                username,
                email,
                password
        );

        MockMultipartFile requestPart = createUserRequestPart(request);

        MvcResult result = mockMvc.perform(multipart("/api/users")
                                .file(requestPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);

        return UUID.fromString(responseJson.get("id").asText());
    }

    private MockMultipartFile createUserRequestPart(UserCreateRequest request) throws Exception {
        return new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private MockMultipartFile createUserUpdateRequestPart(UserUpdateRequest request) throws Exception {
        return new MockMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }
}
