package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.AdminInitializer;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
class ChannelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChannelRepository channelRepository;

    @MockitoBean
    private AdminInitializer adminInitializer;

    @Nested
    @DisplayName("공개 채널 생성 API")
    class CreatePublicChannel {

        @Test
        @DisplayName("CHANNEL_MANAGER 권한이면 공개 채널을 생성하고 201을 반환")
        void create_success() throws Exception {
            // given
            PublicChannelCreateRequest request =
                    new PublicChannelCreateRequest(
                            "공지",
                            "전체 공지 채널"
                    );

            DiscodeitUserDetails principal =
                    createPrincipal(Role.CHANNEL_MANAGER);

            // when & then
            mockMvc.perform(
                            post("/api/channels/public")
                                    .with(user(principal))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$.name")
                            .value("공지"));

            assertThat(channelRepository.findAll())
                    .hasSize(1);

            Channel savedChannel =
                    channelRepository.findAll().get(0);

            assertThat(savedChannel.getType())
                    .isEqualTo(ChannelType.PUBLIC);

            assertThat(savedChannel.getName())
                    .isEqualTo("공지");

            assertThat(savedChannel.getDescription())
                    .isEqualTo("전체 공지 채널");
        }

        @Test
        @DisplayName("USER 권한이면 공개 채널 생성 시 403을 반환")
        void create_fail_forbidden() throws Exception {
            // given
            PublicChannelCreateRequest request =
                    new PublicChannelCreateRequest(
                            "공지",
                            "전체 공지 채널"
                    );

            DiscodeitUserDetails principal =
                    createPrincipal(Role.USER);

            // when & then
            mockMvc.perform(
                            post("/api/channels/public")
                                    .with(user(principal))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden());

            assertThat(channelRepository.findAll())
                    .isEmpty();
        }

        @Test
        @DisplayName("채널 이름이 비어 있으면 400을 반환")
        void create_fail_invalid_input() throws Exception {
            // given
            PublicChannelCreateRequest request =
                    new PublicChannelCreateRequest(
                            "",
                            "전체 공지 채널"
                    );

            DiscodeitUserDetails principal =
                    createPrincipal(Role.CHANNEL_MANAGER);

            // when & then
            mockMvc.perform(
                            post("/api/channels/public")
                                    .with(user(principal))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value("INVALID_INPUT_VALUE"));

            assertThat(channelRepository.findAll())
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("공개 채널 수정 API")
    class UpdateChannel {

        @Test
        @DisplayName("CHANNEL_MANAGER 권한이면 공개 채널을 수정하고 200을 반환")
        void update_success() throws Exception {
            // given
            Channel savedChannel = savePublicChannel(
                    "기존 공지",
                    "기존 설명"
            );

            PublicChannelUpdateRequest request =
                    new PublicChannelUpdateRequest(
                            "수정된 공지",
                            "수정된 설명"
                    );

            DiscodeitUserDetails principal =
                    createPrincipal(Role.CHANNEL_MANAGER);

            // when & then
            mockMvc.perform(
                            patch(
                                    "/api/channels/{channelId}",
                                    savedChannel.getId()
                            )
                                    .with(user(principal))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(savedChannel.getId().toString()))
                    .andExpect(jsonPath("$.type")
                            .value("PUBLIC"))
                    .andExpect(jsonPath("$.name")
                            .value("수정된 공지"));

            Channel updatedChannel = channelRepository
                    .findById(savedChannel.getId())
                    .orElseThrow();

            assertThat(updatedChannel.getName())
                    .isEqualTo("수정된 공지");

            assertThat(updatedChannel.getDescription())
                    .isEqualTo("수정된 설명");
        }

        @Test
        @DisplayName("USER 권한이면 공개 채널 수정 시 403을 반환")
        void update_fail_forbidden() throws Exception {
            // given
            Channel savedChannel = savePublicChannel(
                    "기존 공지",
                    "기존 설명"
            );

            PublicChannelUpdateRequest request =
                    new PublicChannelUpdateRequest(
                            "수정된 공지",
                            "수정된 설명"
                    );

            DiscodeitUserDetails principal =
                    createPrincipal(Role.USER);

            // when & then
            mockMvc.perform(
                            patch(
                                    "/api/channels/{channelId}",
                                    savedChannel.getId()
                            )
                                    .with(user(principal))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isForbidden());

            Channel unchangedChannel = channelRepository
                    .findById(savedChannel.getId())
                    .orElseThrow();

            assertThat(unchangedChannel.getName())
                    .isEqualTo("기존 공지");
        }

        @Test
        @DisplayName("존재하지 않는 채널을 수정하면 404를 반환")
        void update_fail_not_found() throws Exception {
            // given
            UUID unknownChannelId = UUID.randomUUID();

            PublicChannelUpdateRequest request =
                    new PublicChannelUpdateRequest(
                            "수정된 공지",
                            "수정된 설명"
                    );

            DiscodeitUserDetails principal =
                    createPrincipal(Role.CHANNEL_MANAGER);

            // when & then
            mockMvc.perform(
                            patch(
                                    "/api/channels/{channelId}",
                                    unknownChannelId
                            )
                                    .with(user(principal))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("CHANNEL_NOT_FOUND"))
                    .andExpect(jsonPath("$.details.channelId")
                            .value(unknownChannelId.toString()));

            assertThat(channelRepository.findById(unknownChannelId))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("채널 삭제 API")
    class DeleteChannel {

        @Test
        @DisplayName("CHANNEL_MANAGER 권한이면 채널을 삭제하고 204를 반환")
        void delete_success() throws Exception {
            // given
            Channel savedChannel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            UUID channelId = savedChannel.getId();

            DiscodeitUserDetails principal =
                    createPrincipal(Role.CHANNEL_MANAGER);

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/channels/{channelId}",
                                    channelId
                            )
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isNoContent());

            assertThat(channelRepository.findById(channelId))
                    .isEmpty();
        }

        @Test
        @DisplayName("USER 권한이면 채널 삭제 시 403을 반환")
        void delete_fail_forbidden() throws Exception {
            // given
            Channel savedChannel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            UUID channelId = savedChannel.getId();

            DiscodeitUserDetails principal =
                    createPrincipal(Role.USER);

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/channels/{channelId}",
                                    channelId
                            )
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isForbidden());

            assertThat(channelRepository.findById(channelId))
                    .isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 채널을 삭제하면 404를 반환")
        void delete_fail_not_found() throws Exception {
            // given
            UUID unknownChannelId = UUID.randomUUID();

            DiscodeitUserDetails principal =
                    createPrincipal(Role.CHANNEL_MANAGER);

            // when & then
            mockMvc.perform(
                            delete(
                                    "/api/channels/{channelId}",
                                    unknownChannelId
                            )
                                    .with(user(principal))
                                    .with(csrf())
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("CHANNEL_NOT_FOUND"))
                    .andExpect(jsonPath("$.details.channelId")
                            .value(unknownChannelId.toString()));

            assertThat(channelRepository.findById(unknownChannelId))
                    .isEmpty();
        }
    }

    private DiscodeitUserDetails createPrincipal(Role role) {
        UserDto userDto = new UserDto(
                UUID.randomUUID(),
                "test-user",
                "test@test.com",
                null,
                true,
                role
        );

        return new DiscodeitUserDetails(
                userDto,
                "encoded-password"
        );
    }

    private Channel savePublicChannel(
            String name,
            String description
    ) {
        Channel channel = new Channel(
                ChannelType.PUBLIC,
                name,
                description
        );

        return channelRepository.saveAndFlush(channel);
    }
}