package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID createPublicChannelAndGetId(String name) throws Exception {
        String response = mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PublicChannelCreateRequest(name, "테스트 채널입니다"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }


    @Test
    @DisplayName("PUBLIC 채널 생성 성공 - 유효한 요청이면 201과 채널 정보를 반환한다")
    void createPublicChannel_성공() throws Exception {
        // when & then
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PublicChannelCreateRequest("공개채널", "채널 설명"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("공개채널"))
                .andExpect(jsonPath("$.type").value("PUBLIC"));
    }

    @Test
    @DisplayName("PUBLIC 채널 생성 실패 - 채널 이름이 없으면 400을 반환한다")
    void createPublicChannel_실패_이름없음() throws Exception {
        // when & then
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"설명\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.name").exists());
    }


    @Test
    @DisplayName("채널 수정 성공 - PUBLIC 채널의 이름과 설명을 수정하면 200을 반환한다")
    void updateChannel_성공() throws Exception {
        // given: 채널 생성
        UUID channelId = createPublicChannelAndGetId("원래이름");

        // when & then
        mockMvc.perform(patch("/api/channels/{channelId}", channelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PublicChannelUpdateRequest("새이름", "새설명"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새이름"))
                .andExpect(jsonPath("$.description").value("새설명"));
    }

    @Test
    @DisplayName("채널 수정 실패 - 존재하지 않는 채널이면 404를 반환한다")
    void updateChannel_실패_채널없음() throws Exception {
        // given
        UUID notExistId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/channels/{channelId}", notExistId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PublicChannelUpdateRequest("새이름", "새설명"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
    }


    @Test
    @DisplayName("채널 삭제 성공 - 존재하는 채널을 삭제하면 204를 반환한다")
    void deleteChannel_성공() throws Exception {
        // given: 채널 생성
        UUID channelId = createPublicChannelAndGetId("삭제할채널");

        // when & then
        mockMvc.perform(delete("/api/channels/{channelId}", channelId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("채널 삭제 실패 - 존재하지 않는 채널이면 404를 반환한다")
    void deleteChannel_실패_채널없음() throws Exception {
        // given
        UUID notExistId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/channels/{channelId}", notExistId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
    }
}