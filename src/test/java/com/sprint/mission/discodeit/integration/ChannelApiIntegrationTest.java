package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("PUBLIC 채널 생성, 조회, 수정, 삭제 API가 동작한다")
    void publicChannelApi_lifecycle_success() throws Exception {
        String channelId = createPublicChannel("general", "general channel");

        mockMvc.perform(get("/api/channels/{channelId}", channelId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId))
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name").value("general"));

        ChannelUpdateRequest updateRequest = new ChannelUpdateRequest(
                UUID.fromString(channelId),
                "notice",
                "notice channel"
        );

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId))
                .andExpect(jsonPath("$.name").value("notice"))
                .andExpect(jsonPath("$.description").value("notice channel"));

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PRIVATE 채널을 생성하고 사용자별 채널 목록에서 조회한다")
    void privateChannelApi_createAndFindByUser_success() throws Exception {
        String userId1 = createUser("user1", "user1@example.com", "password");
        String userId2 = createUser("user2", "user2@example.com", "password");

        PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
                List.of(UUID.fromString(userId1), UUID.fromString(userId2))
        );

        String privateChannelId = objectMapper.readTree(
                        mockMvc.perform(post("/api/channels/private")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.type").value("PRIVATE"))
                                .andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .get("id")
                .asText();

        mockMvc.perform(get("/api/channels")
                        .param("userId", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + privateChannelId + "')]").exists());
    }

    @Test
    @DisplayName("PUBLIC 채널 생성 요청 값이 올바르지 않으면 400을 반환한다")
    void createPublicChannel_fail_validation() throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(
                "",
                "description"
        );

        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("존재하지 않는 채널 조회 시 404를 반환한다")
    void findChannel_fail_notFound() throws Exception {
        UUID unknownChannelId = UUID.randomUUID();

        mockMvc.perform(get("/api/channels/{channelId}", unknownChannelId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    private String createPublicChannel(String name, String description) throws Exception {
        PublicChannelCreateRequest request = new PublicChannelCreateRequest(name, description);

        return objectMapper.readTree(
                        mockMvc.perform(post("/api/channels/public")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsBytes(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value(name))
                                .andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .get("id")
                .asText();
    }

    private String createUser(String username, String email, String password) throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                username,
                email,
                password,
                null,
                null,
                null
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "userCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        return objectMapper.readTree(
                        mockMvc.perform(multipart("/api/users")
                                        .file(requestPart))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.username").value(username))
                                .andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .get("id")
                .asText();
    }
}