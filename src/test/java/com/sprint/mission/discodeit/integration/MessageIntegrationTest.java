package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 메시지 테스트에 필요한 User와 Channel의 ID
    private UUID authorId;
    private UUID channelId;

    @BeforeEach
    void setUp() throws Exception {
        // 유저 생성
        MockMultipartFile userRequest = new MockMultipartFile(
                "userCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(
                        new UserCreateRequest("author", "author@test.com", "password1234"))
        );
        String userResponse = mockMvc.perform(multipart("/api/users").file(userRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        authorId = UUID.fromString(objectMapper.readTree(userResponse).get("id").asText());

        // 채널 생성
        String channelResponse = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PublicChannelCreateRequest("테스트채널", "채널설명"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        channelId = UUID.fromString(objectMapper.readTree(channelResponse).get("id").asText());
    }

    private UUID createMessageAndGetId(String content) throws Exception {
        MockMultipartFile msgRequest = new MockMultipartFile(
                "messageCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(
                        new MessageCreateRequest(content, channelId, authorId))
        );

        String response = mockMvc.perform(multipart("/api/messages").file(msgRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }


    @Test
    @DisplayName("메시지 생성 성공 - 유효한 요청이면 201과 메시지 정보를 반환한다")
    void createMessage_성공() throws Exception {
        // when & then
        MockMultipartFile msgRequest = new MockMultipartFile(
                "messageCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(
                        new MessageCreateRequest("안녕하세요!", channelId, authorId))
        );

        mockMvc.perform(multipart("/api/messages").file(msgRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("안녕하세요!"))
                .andExpect(jsonPath("$.channelId").value(channelId.toString()))
                .andExpect(jsonPath("$.author.username").value("author"));
    }

    @Test
    @DisplayName("메시지 생성 실패 - 존재하지 않는 채널이면 404를 반환한다")
    void createMessage_실패_채널없음() throws Exception {
        // given
        UUID notExistChannelId = UUID.randomUUID();
        MockMultipartFile msgRequest = new MockMultipartFile(
                "messageCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(
                        new MessageCreateRequest("메시지", notExistChannelId, authorId))
        );

        // when & then
        mockMvc.perform(multipart("/api/messages").file(msgRequest))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("메시지 목록 조회 성공 - 채널의 메시지를 PageResponse로 반환한다")
    void findAllMessages_성공() throws Exception {
        // given
        createMessageAndGetId("첫 번째 메시지");
        createMessageAndGetId("두 번째 메시지");

        // when & then
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("메시지 목록 조회 - 메시지가 없으면 빈 content를 반환한다")
    void findAllMessages_빈목록() throws Exception {

        // when & then
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }


    @Test
    @DisplayName("메시지 수정 성공 - 존재하는 메시지의 내용을 수정하면 200을 반환한다")
    void updateMessage_성공() throws Exception {
        // given: 메시지 생성
        UUID messageId = createMessageAndGetId("원래 내용");

        // when & then
        mockMvc.perform(patch("/api/messages/{messageId}", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MessageUpdateRequest("수정된 내용"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("메시지 수정 실패 - 존재하지 않는 메시지면 404를 반환한다")
    void updateMessage_실패_메시지없음() throws Exception {
        // given
        UUID notExistId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/messages/{messageId}", notExistId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new MessageUpdateRequest("수정 내용"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
    }


    @Test
    @DisplayName("메시지 삭제 성공 - 존재하는 메시지를 삭제하면 204를 반환한다")
    void deleteMessage_성공() throws Exception {
        // given
        UUID messageId = createMessageAndGetId("삭제할 메시지");

        // when & then
        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 존재하지 않는 메시지면 404를 반환한다")
    void deleteMessage_실패_메시지없음() throws Exception {
        // given
        UUID notExistId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/messages/{messageId}", notExistId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
    }
}