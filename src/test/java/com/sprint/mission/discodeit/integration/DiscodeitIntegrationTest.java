package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.ChannelPublicRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
@EnableJpaAuditing
@Transactional
class DiscodeitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 사용자_API_생성_목록조회_수정_삭제_성공() throws Exception {
        UUID userId = createUser(
                "codeit",
                "codeit@example.com",
                "password123"
        );

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("codeit"))
                .andExpect(jsonPath("$[0].email").value("codeit@example.com"));

        UserRequest updateRequest =
                new UserRequest("updated", "updated@example.com", "newPassword123");
        MockMultipartFile updatePart = jsonPart("userUpdateRequest", updateRequest);

        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .file(updatePart)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void 사용자_API_중복된_사용자_이름이면_409를_반환한다() throws Exception {
        createUser("codeit", "first@example.com", "password123");

        UserRequest duplicateRequest =
                new UserRequest("codeit", "second@example.com", "password123");
        MockMultipartFile requestPart =
                jsonPart("userCreateRequest", duplicateRequest);

        mockMvc.perform(multipart("/api/users").file(requestPart))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.details.field").value("username"))
                .andExpect(jsonPath("$.details.value").value("codeit"));
    }

    @Test
    void 채널_API_생성_수정_삭제_성공() throws Exception {
        UUID channelId = createPublicChannel("general", "일반 채널입니다.");

        ChannelPublicRequest updateRequest =
                new ChannelPublicRequest("announcement", "공지 채널입니다.");

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(channelId.toString()))
                .andExpect(jsonPath("$.name").value("announcement"))
                .andExpect(jsonPath("$.description").value("공지 채널입니다."))
                .andExpect(jsonPath("$.type").value("PUBLIC"));

        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/channels")
                        .param("userId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void 채널_API_존재하지_않는_채널을_수정하면_404를_반환한다()
            throws Exception {
        UUID channelId = UUID.randomUUID();
        ChannelPublicRequest request =
                new ChannelPublicRequest("general", "일반 채널입니다.");

        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.details.channelId").value(channelId.toString()));
    }

    @Test
    void 메시지_API_생성_목록조회_수정_삭제_성공() throws Exception {
        UUID userId = createUser(
                "message-user",
                "message@example.com",
                "password123"
        );
        UUID channelId =
                createPublicChannel("message-channel", "메시지 테스트 채널입니다.");

        MessageCreateRequest createRequest = new MessageCreateRequest(
                channelId,
                userId,
                "안녕하세요.",
                List.of()
        );
        MockMultipartFile createPart =
                jsonPart("messageCreateRequest", createRequest);

        MvcResult createResult = mockMvc.perform(
                        multipart("/api/messages").file(createPart)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.senderId").value(userId.toString()))
                .andExpect(jsonPath("$.content").value("안녕하세요."))
                .andReturn();
        UUID messageId = extractId(createResult);

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString())
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.content[0].id").value(messageId.toString()))
                .andExpect(jsonPath("$.content[0].content").value("안녕하세요."));

        MessageUpdateRequest updateRequest =
                new MessageUpdateRequest("수정된 메시지입니다.");

        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(messageId.toString()))
                .andExpect(jsonPath("$.content").value("수정된 메시지입니다."));

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString())
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void 메시지_API_존재하지_않는_채널에_생성하면_404를_반환한다()
            throws Exception {
        UUID userId = createUser(
                "message-user",
                "message@example.com",
                "password123"
        );
        UUID channelId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                channelId,
                userId,
                "안녕하세요.",
                List.of()
        );
        MockMultipartFile requestPart =
                jsonPart("messageCreateRequest", request);

        mockMvc.perform(multipart("/api/messages").file(requestPart))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.details.channelId").value(channelId.toString()));
    }

    private UUID createUser(
            String username,
            String email,
            String password
    ) throws Exception {
        UserRequest request = new UserRequest(username, email, password);
        MockMultipartFile requestPart =
                jsonPart("userCreateRequest", request);

        MvcResult result = mockMvc.perform(
                        multipart("/api/users").file(requestPart)
                )
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private UUID createPublicChannel(
            String name,
            String description
    ) throws Exception {
        ChannelPublicRequest request =
                new ChannelPublicRequest(name, description);

        MvcResult result = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private MockMultipartFile jsonPart(
            String name,
            Object value
    ) throws Exception {
        return new MockMultipartFile(
                name,
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(value)
        );
    }

    private UUID extractId(MvcResult result) throws Exception {
        JsonNode response =
                objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return UUID.fromString(response.get("id").asText());
    }
}