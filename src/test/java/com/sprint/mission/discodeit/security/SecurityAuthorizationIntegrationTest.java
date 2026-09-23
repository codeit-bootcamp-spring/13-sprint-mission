package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.command.channel.ChannelCreatePublicCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.request.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security 인증·인가 통합 테스트")
class SecurityAuthorizationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RoleHierarchy roleHierarchy;

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    UserRoleUpdater userRoleUpdater;

    @Test
    @DisplayName("보호된 API에 미인증으로 접근하면 401을 반환한다")
    void protectedApi_returnsUnauthorized_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpectAll(
                        status().isUnauthorized(),
                        content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
                        jsonPath("$.status").value(401),
                        jsonPath("$.code").value("AUTH_401")
                );
    }

    @Test
    @DisplayName("Swagger, OpenAPI 문서와 정적 리소스는 인증 없이 접근할 수 있다")
    void nonApiResources_arePublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v3/api/docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Actuator는 미인증 사용자에게 401을 반환한다")
    void actuator_returnsUnauthorized_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Actuator는 일반 사용자에게 403을 반환한다")
    void actuator_returnsForbidden_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Actuator는 관리자에게 접근을 허용한다")
    void actuator_returnsOk_whenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자는 공개 채널을 생성할 수 없다")
    void createPublicChannel_returnsForbidden_whenUserHasUserRole() throws Exception {
        performPublicChannelCreate()
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.status").value(403),
                        jsonPath("$.code").value("AUTH_403")
                );
    }

    @Test
    @WithMockUser(roles = "CHANNEL_MANAGER")
    @DisplayName("채널 매니저는 공개 채널을 생성할 수 있다")
    void createPublicChannel_returnsCreated_whenUserIsChannelManager() throws Exception {
        performPublicChannelCreate()
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("관리자는 권한 계층을 통해 공개 채널을 생성할 수 있다")
    void createPublicChannel_returnsCreated_whenUserIsAdmin() throws Exception {
        performPublicChannelCreate()
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자는 공개 채널을 수정하거나 삭제할 수 없다")
    void managePublicChannel_returnsForbidden_whenUserHasUserRole() throws Exception {
        Channel channel = savePublicChannel();
        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(
                "updated-security-channel",
                "updated security authorization test"
        );

        mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        mockMvc.perform(delete("/api/channels/{channelId}", channel.getId())
                        .with(csrf()))
                .andExpectAll(
                        status().isForbidden(),
                        jsonPath("$.code").value("AUTH_403")
                );

        assertThat(channelRepository.existsById(channel.getId())).isTrue();
    }

    @Test
    @WithMockUser(roles = "CHANNEL_MANAGER")
    @DisplayName("채널 매니저는 공개 채널을 수정하고 삭제할 수 있다")
    void managePublicChannel_succeeds_whenUserIsChannelManager() throws Exception {
        Channel channel = savePublicChannel();
        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(
                "updated-security-channel",
                "updated security authorization test"
        );

        mockMvc.perform(patch("/api/channels/{channelId}", channel.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/channels/{channelId}", channel.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(channelRepository.existsById(channel.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("일반 사용자가 역할 변경 Service를 직접 호출하면 인가가 거부된다")
    void updateUserRoleService_isDenied_whenUserIsNotAdmin() {
        UserRoleUpdateCommand command = new UserRoleUpdateCommand(Role.CHANNEL_MANAGER);

        assertThatThrownBy(() -> userRoleUpdater.updateRole(UUID.randomUUID(), command))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("관리자와 채널 매니저의 권한 계층을 적용한다")
    void roleHierarchy_containsInheritedRoles() {
        List<String> adminAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                ).stream()
                .map(authority -> authority.getAuthority())
                .toList();
        List<String> channelManagerAuthorities = roleHierarchy.getReachableGrantedAuthorities(
                        List.of(new SimpleGrantedAuthority("ROLE_CHANNEL_MANAGER"))
                ).stream()
                .map(authority -> authority.getAuthority())
                .toList();

        assertThat(adminAuthorities)
                .contains("ROLE_ADMIN", "ROLE_CHANNEL_MANAGER", "ROLE_USER");
        assertThat(channelManagerAuthorities)
                .contains("ROLE_CHANNEL_MANAGER", "ROLE_USER")
                .doesNotContain("ROLE_ADMIN");
    }

    private org.springframework.test.web.servlet.ResultActions performPublicChannelCreate() throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "security-public-channel",
                "security authorization test"
        );

        return mockMvc.perform(post("/api/channels/public")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private Channel savePublicChannel() {
        return channelRepository.save(new Channel(new ChannelCreatePublicCommand(
                "security-public-channel",
                "security authorization test",
                ChannelType.PUBLIC
        )));
    }
}
