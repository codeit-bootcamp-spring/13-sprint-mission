package com.sprint.mission.discodeit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DiscodeitApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자 생성 - 실제로 DB에 저장되고 201 반환")
    void createUser_통합_성공() throws Exception {
        UserCreateRequest request = new UserCreateRequest("박경석", "park@gmail.com", "0000");
        MockMultipartFile jsonPart = new MockMultipartFile(
                "userCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/users")
                        .file(jsonPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("박경석"))
                .andExpect(jsonPath("$.email").value("park@gmail.com"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 - 실제로 404 반환")
    void findUser_통합_실패() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 목록 조회 - 200")
    void findAllUser_통합() throws Exception {
        // 먼저 유저 하나 생성
        UserCreateRequest request = new UserCreateRequest("김철수", "kim@gmail.com", "1234");
        MockMultipartFile jsonPart = new MockMultipartFile(
                "userCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/api/users").file(jsonPart)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // 목록 조회
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("사용자 수정 - 생성 후 수정하면 200")
    void updateUser_통합() throws Exception {
        // 생성
        UserCreateRequest createReq = new UserCreateRequest("수정전", "before@gmail.com", "0000");
        MockMultipartFile createPart = new MockMultipartFile(
                "userCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(createReq));
        String responseBody = mockMvc.perform(multipart("/api/users").file(createPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse().getContentAsString();
        String userId = objectMapper.readTree(responseBody).get("id").asText();

        // 수정 (이름 변경)
        UserUpdateRequest updateReq = new UserUpdateRequest("수정후", null, null);
        MockMultipartFile updatePart = new MockMultipartFile(
                "userUpdateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(updateReq));
        mockMvc.perform(multipart("/api/users/{userId}", userId)
                        .file(updatePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("수정후"));
    }

    @Test
    @DisplayName("사용자 삭제 - 생성 후 삭제하면 204")
    void deleteUser_통합() throws Exception {
        // 생성
        UserCreateRequest createReq = new UserCreateRequest("삭제대상", "del@gmail.com", "0000");
        MockMultipartFile createPart = new MockMultipartFile(
                "userCreateRequest", "", "application/json",
                objectMapper.writeValueAsBytes(createReq));
        String responseBody = mockMvc.perform(multipart("/api/users").file(createPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse().getContentAsString();
        String userId = objectMapper.readTree(responseBody).get("id").asText();

        // 삭제
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isNoContent());
    }


    // ── 헬퍼 메서드 ──
    private String createChannel() throws Exception {
        PublicChannelRequest req = new PublicChannelRequest("공지" + UUID.randomUUID(), "설명");
        String body = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }

    private String createUserAndGetId(String name, String email) throws Exception {
        UserCreateRequest req = new UserCreateRequest(name, email, "0000");
        MockMultipartFile part = new MockMultipartFile(
                "userCreateRequest", "", "application/json", objectMapper.writeValueAsBytes(req));
        String body = mockMvc.perform(multipart("/api/users").file(part)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }

    private String createMessage(String channelId, String authorId) throws Exception {
        MessageCreateRequest req = new MessageCreateRequest(
                UUID.fromString(channelId), UUID.fromString(authorId), "안녕");
        MockMultipartFile part = new MockMultipartFile(
                "messageCreateRequest", "", "application/json", objectMapper.writeValueAsBytes(req));
        String body = mockMvc.perform(multipart("/api/messages").file(part)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asText();
    }

    // ── 채널 통합 테스트 2개 ──
    @Test
    @DisplayName("채널 생성 - 201")
    void createChannel_통합() throws Exception {
        PublicChannelRequest req = new PublicChannelRequest("공지", "공지 채널");
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("공지"));
    }

    @Test
    @DisplayName("채널 삭제 - 생성 후 삭제하면 204")
    void deleteChannel_통합() throws Exception {
        String channelId = createChannel();
        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());
    }

    // ── 메시지 통합 테스트 2개 ──
    @Test
    @DisplayName("메시지 생성 - 201")
    void createMessage_통합() throws Exception {
        String authorId = createUserAndGetId("작성자", "author@gmail.com");
        String channelId = createChannel();

        MessageCreateRequest req = new MessageCreateRequest(
                UUID.fromString(channelId), UUID.fromString(authorId), "안녕하세요");
        MockMultipartFile part = new MockMultipartFile(
                "messageCreateRequest", "", "application/json", objectMapper.writeValueAsBytes(req));

        mockMvc.perform(multipart("/api/messages").file(part)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("안녕하세요"));
    }

    @Test
    @DisplayName("메시지 삭제 - 생성 후 삭제하면 204")
    void deleteMessage_통합() throws Exception {
        String authorId = createUserAndGetId("작성자2", "author2@gmail.com");
        String channelId = createChannel();
        String messageId = createMessage(channelId, authorId);

        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());
    }
}
