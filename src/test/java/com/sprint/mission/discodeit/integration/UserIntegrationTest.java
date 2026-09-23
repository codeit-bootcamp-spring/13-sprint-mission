package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /*
     * UserIntegrationTest에서는 AdminInitializer 자체를 테스트하는 것이 아니므로
     * ADMIN 계정 자동 생성으로 사용자 수 검증이 영향을 받지 않도록 Mock 처리
     */
    @MockitoBean
    private AdminInitializer adminInitializer;

    @Nested
    @DisplayName("사용자 생성 API")
    class CreateUser {

        @Test
        @DisplayName("정상적인 요청이면 사용자를 생성하고 201을 반환")
        void create_success() throws Exception {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            MockMultipartFile requestPart =
                    createUserRequestPart(request);

            // when & then
            mockMvc.perform(
                            multipart("/api/users")
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(csrf())
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.username")
                            .value("user1"))
                    .andExpect(jsonPath("$.email")
                            .value("user1@test.com"))
                    .andExpect(jsonPath("$.online")
                            .value(false))
                    .andExpect(jsonPath("$.role")
                            .value("USER"));

            User savedUser = userRepository
                    .findByUsername("user1")
                    .orElseThrow();

            assertThat(savedUser.getUsername())
                    .isEqualTo("user1");

            assertThat(savedUser.getEmail())
                    .isEqualTo("user1@test.com");

            assertThat(savedUser.getRole())
                    .isEqualTo(Role.USER);

            // 평문이 그대로 저장되지 않았는지 확인
            assertThat(savedUser.getPassword())
                    .isNotEqualTo("password1");

            // BCrypt로 정상 인코딩되었는지 확인
            assertThat(
                    passwordEncoder.matches(
                            "password1",
                            savedUser.getPassword()
                    )
            ).isTrue();
        }

        @Test
        @DisplayName("중복된 이메일로 생성하면 409를 반환")
        void create_fail() throws Exception {
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

            MockMultipartFile requestPart =
                    createUserRequestPart(duplicateRequest);

            // when & then
            mockMvc.perform(
                            multipart("/api/users")
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(csrf())
                    )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code")
                            .value("DUPLICATE_USER_EMAIL"));

            assertThat(userRepository.findAll())
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("사용자 수정 API")
    class UpdateUser {

        @Test
        @DisplayName("본인이 요청하면 사용자 정보를 수정하고 200을 반환")
        void update_success() throws Exception {
            // given
            UUID userId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            DiscodeitUserDetails principal = createPrincipal(
                    userId,
                    "user1",
                    "user1@test.com",
                    Role.USER
            );

            UserUpdateRequest request = new UserUpdateRequest(
                    "updated-user",
                    "updated@test.com",
                    "updated-password"
            );

            MockMultipartFile requestPart =
                    createUserUpdateRequestPart(request);

            // when & then
            mockMvc.perform(
                            multipart("/api/users/{userId}", userId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(user(principal))
                                    .with(csrf())
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
                            .value("updated@test.com"))
                    .andExpect(jsonPath("$.role")
                            .value("USER"));

            User updatedUser = userRepository
                    .findById(userId)
                    .orElseThrow();

            assertThat(updatedUser.getUsername())
                    .isEqualTo("updated-user");

            assertThat(updatedUser.getEmail())
                    .isEqualTo("updated@test.com");

            // 수정된 비밀번호도 평문이 아닌 BCrypt로 저장되는지 확인
            assertThat(updatedUser.getPassword())
                    .isNotEqualTo("updated-password");

            assertThat(
                    passwordEncoder.matches(
                            "updated-password",
                            updatedUser.getPassword()
                    )
            ).isTrue();
        }

        @Test
        @DisplayName("다른 사용자의 정보를 수정하면 403을 반환")
        void update_fail_not_owner() throws Exception {
            // given
            UUID targetUserId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            UUID otherUserId = UUID.randomUUID();

            DiscodeitUserDetails principal = createPrincipal(
                    otherUserId,
                    "other-user",
                    "other@test.com",
                    Role.USER
            );

            UserUpdateRequest request = new UserUpdateRequest(
                    "updated-user",
                    "updated@test.com",
                    "updated-password"
            );

            MockMultipartFile requestPart =
                    createUserUpdateRequestPart(request);

            // when & then
            mockMvc.perform(
                            multipart("/api/users/{userId}", targetUserId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(user(principal))
                                    .with(csrf())
                                    .with(servletRequest -> {
                                        servletRequest.setMethod("PATCH");
                                        return servletRequest;
                                    })
                    )
                    .andExpect(status().isForbidden());

            User unchangedUser = userRepository
                    .findById(targetUserId)
                    .orElseThrow();

            assertThat(unchangedUser.getUsername())
                    .isEqualTo("user1");
        }

        @Test
        @DisplayName("본인이 요청했지만 사용자가 존재하지 않으면 404를 반환")
        void update_fail_not_found() throws Exception {
            // given
            UUID unknownUserId = UUID.randomUUID();

            /*
             * @PreAuthorize에서
             * #userId == authentication.principal.userDto.id
             * 검사가 먼저 수행되므로 같은 ID의 Principal을 사용
             */
            DiscodeitUserDetails principal = createPrincipal(
                    unknownUserId,
                    "unknown",
                    "unknown@test.com",
                    Role.USER
            );

            UserUpdateRequest request = new UserUpdateRequest(
                    "updated-user",
                    "updated@test.com",
                    "updated-password"
            );

            MockMultipartFile requestPart =
                    createUserUpdateRequestPart(request);

            // when & then
            mockMvc.perform(
                            multipart("/api/users/{userId}", unknownUserId)
                                    .file(requestPart)
                                    .contentType(MediaType.MULTIPART_FORM_DATA)
                                    .with(user(principal))
                                    .with(csrf())
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
        @DisplayName("본인이 요청하면 사용자를 삭제하고 204를 반환")
        void delete_success() throws Exception {
            // given
            UUID userId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            DiscodeitUserDetails principal = createPrincipal(
                    userId,
                    "user1",
                    "user1@test.com",
                    Role.USER
            );

            // when & then
            mockMvc.perform(
                            delete("/api/users/{userId}", userId)
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isNoContent());

            assertThat(userRepository.findById(userId))
                    .isEmpty();
        }

        @Test
        @DisplayName("다른 사용자를 삭제하면 403을 반환")
        void delete_fail_not_owner() throws Exception {
            // given
            UUID targetUserId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            UUID otherUserId = UUID.randomUUID();

            DiscodeitUserDetails principal = createPrincipal(
                    otherUserId,
                    "other-user",
                    "other@test.com",
                    Role.USER
            );

            // when & then
            mockMvc.perform(
                            delete("/api/users/{userId}", targetUserId)
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isForbidden());

            assertThat(userRepository.findById(targetUserId))
                    .isPresent();
        }

        @Test
        @DisplayName("본인이 요청했지만 사용자가 존재하지 않으면 404를 반환")
        void delete_fail_not_found() throws Exception {
            // given
            UUID unknownUserId = UUID.randomUUID();

            DiscodeitUserDetails principal = createPrincipal(
                    unknownUserId,
                    "unknown",
                    "unknown@test.com",
                    Role.USER
            );

            // when & then
            mockMvc.perform(
                            delete("/api/users/{userId}", unknownUserId)
                                    .with(user(principal))
                                    .with(csrf())
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
    @DisplayName("사용자 목록 조회 API")
    class FindAllUsers {

        @Test
        @DisplayName("인증된 사용자는 저장된 모든 사용자 목록을 조회")
        void get_success() throws Exception {
            // given
            UUID firstUserId = createUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            createUser(
                    "user2",
                    "user2@test.com",
                    "password2"
            );

            DiscodeitUserDetails principal = createPrincipal(
                    firstUserId,
                    "user1",
                    "user1@test.com",
                    Role.USER
            );

            // when & then
            mockMvc.perform(
                            get("/api/users")
                                    .with(user(principal))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()")
                            .value(2))
                    .andExpect(jsonPath("$[0].username")
                            .exists())
                    .andExpect(jsonPath("$[0].email")
                            .exists())
                    .andExpect(jsonPath("$[0].role")
                            .exists())
                    .andExpect(jsonPath("$[1].username")
                            .exists())
                    .andExpect(jsonPath("$[1].email")
                            .exists())
                    .andExpect(jsonPath("$[1].role")
                            .exists());

            assertThat(userRepository.findAll())
                    .hasSize(2);
        }

        @Test
        @DisplayName("인증된 사용자가 조회하고 저장된 사용자가 없으면 빈 목록을 반환")
        void get_empty() throws Exception {
            // given
            UUID principalId = UUID.randomUUID();

            /*
             * 여기서는 인증 정보만 필요하므로
             * Principal이 반드시 DB에 존재할 필요는 없음
             */
            DiscodeitUserDetails principal = createPrincipal(
                    principalId,
                    "user1",
                    "user1@test.com",
                    Role.USER
            );

            // when & then
            mockMvc.perform(
                            get("/api/users")
                                    .with(user(principal))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            assertThat(userRepository.findAll())
                    .isEmpty();
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 조회하면 401을 반환")
        void get_fail_unauthenticated() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    private UUID createUser(
            String username,
            String email,
            String password
    ) throws Exception {

        UserCreateRequest request = new UserCreateRequest(
                username,
                email,
                password
        );

        MockMultipartFile requestPart =
                createUserRequestPart(request);

        MvcResult result = mockMvc.perform(
                        multipart("/api/users")
                                .file(requestPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(csrf())
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

    private DiscodeitUserDetails createPrincipal(
            UUID userId,
            String username,
            String email,
            Role role
    ) {
        UserDto userDto = new UserDto(
                userId,
                username,
                email,
                null,
                true,
                role
        );

        return new DiscodeitUserDetails(
                userDto,
                "encoded-password"
        );
    }

    private MockMultipartFile createUserRequestPart(
            UserCreateRequest request
    ) throws Exception {

        return new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private MockMultipartFile createUserUpdateRequestPart(
            UserUpdateRequest request
    ) throws Exception {

        return new MockMultipartFile(
                "userUpdateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }
}
