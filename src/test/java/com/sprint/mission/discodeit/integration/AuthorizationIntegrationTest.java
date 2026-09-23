package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 인증하지_않은_API_요청은_401을_반환한다()
            throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void API가_아닌_Actuator_요청은_인증하지_않아도_된다()
            throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void USER는_퍼블릭_채널을_생성할_수_없다()
            throws Exception {
        createPublicChannelAs("USER")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(
                        jsonPath("$.code")
                                .value("ACCESS_DENIED")
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHANNEL_MANAGER", "ADMIN"})
    void CHANNEL_MANAGER와_ADMIN은_퍼블릭_채널을_생성할_수_있다(
            String role
    ) throws Exception {
        createPublicChannelAs(role)
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.type")
                                .value("PUBLIC")
                );
    }

    @Test
    void USER는_사용자_권한을_변경할_수_없다()
            throws Exception {
        User target = saveTargetUser();

        updateRoleAs(
                target,
                "USER",
                UserRole.CHANNEL_MANAGER
        )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.code")
                                .value("ACCESS_DENIED")
                );
    }

    @Test
    void ADMIN은_사용자_권한을_변경할_수_있다()
            throws Exception {
        User target = saveTargetUser();

        updateRoleAs(
                target,
                "ADMIN",
                UserRole.CHANNEL_MANAGER
        )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(target.getId().toString())
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("CHANNEL_MANAGER")
                );
    }

    private ResultActions createPublicChannelAs(
            String role
    ) throws Exception {
        ChannelPublicRequest request =
                new ChannelPublicRequest(
                        "general-" + role.toLowerCase(),
                        "권한 테스트 채널"
                );

        return mockMvc.perform(
                post("/api/channels/public")
                        .with(csrf())
                        .with(
                                user("tester")
                                        .roles(role)
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsBytes(
                                        request
                                )
                        )
        );
    }

    private ResultActions updateRoleAs(
            User target,
            String requesterRole,
            UserRole newRole
    ) throws Exception {
        UserRoleUpdateRequest request =
                new UserRoleUpdateRequest(
                        target.getId(),
                        newRole
                );

        return mockMvc.perform(
                put("/api/auth/role")
                        .with(csrf())
                        .with(
                                user("tester")
                                        .roles(requesterRole)
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsBytes(
                                        request
                                )
                        )
        );
    }

    private User saveTargetUser() {
        return userRepository.saveAndFlush(
                new User(
                        "target",
                        "target@example.com",
                        "encoded-password"
                )
        );
    }
}