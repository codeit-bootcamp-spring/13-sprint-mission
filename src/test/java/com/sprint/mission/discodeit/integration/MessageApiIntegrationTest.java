package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("메시지 생성, 조회, 수정, 삭제 API가 동작한다")
    void messageApi_lifecycle_success() throws Exception {
        String userId = createUser("tester", "tester@example.com", "Password1!");
        String channelId = createPublicChannel("general", "general channel");
        String messageId = createMessage("hello", channelId, userId);

        mockMvc.perform(get("/api/messages/{messageId}", messageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.channelId").value(channelId));

        MessageUpdateRequest updateRequest = new MessageUpdateRequest(
                UUID.fromString(messageId),
                "updated message"
        );

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId))
                .andExpect(jsonPath("$.content").value("updated message"));

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("채널의 메시지 목록을 조회한다")
    void findMessagesByChannel_success() throws Exception {
        String userId = createUser("tester", "tester@example.com", "password");
        String channelId = createPublicChannel("general", "general channel");

        createMessage("first message", channelId, userId);
        createMessage("second message", channelId, userId);

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    @DisplayName("메시지 생성 요청 값이 올바르지 않으면 400을 반환한다")
    void createMessage_fail_validation() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                null,
                null,
                List.of()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        mockMvc.perform(multipart("/api/messages")
                        .file(requestPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("존재하지 않는 메시지 조회 시 404를 반환한다")
    void findMessage_fail_notFound() throws Exception {
        UUID unknownMessageId = UUID.randomUUID();

        mockMvc.perform(get("/api/messages/{messageId}", unknownMessageId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    private String createMessage(String content, String channelId, String userId) throws Exception {
        MessageCreateRequest request = new MessageCreateRequest(
                content,
                UUID.fromString(channelId),
                UUID.fromString(userId),
                List.of()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        return objectMapper.readTree(
                        mockMvc.perform(multipart("/api/messages")
                                        .file(requestPart))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.content").value(content))
                                .andReturn()
                                .getResponse()
                                .getContentAsString()
                )
                .get("id")
                .asText();
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
