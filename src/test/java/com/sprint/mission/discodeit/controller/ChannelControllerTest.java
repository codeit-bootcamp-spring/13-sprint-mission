package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ChannelController.class)
@ActiveProfiles("test")
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChannelService channelService;


    @Test
    @DisplayName("POST /api/channels/public - 유효한 요청이면 201과 채널을 반환한다")
    void createPublic_성공() throws Exception {
        // given
        PublicChannelCreateRequest request = new PublicChannelCreateRequest("테스트채널", "설명입니다");
        ChannelDto response = new ChannelDto(
                UUID.randomUUID(),     // id
                ChannelType.PUBLIC,    // type
                "테스트채널",           // name
                "설명입니다",           // description
                List.<UserDto>of(),    // participants
                Instant.now()          // lastMessageAt
        );
        given(channelService.create(any(PublicChannelCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트채널"))
                .andExpect(jsonPath("$.type").value("PUBLIC"));
    }

    @Test
    @DisplayName("POST /api/channels/public - 채널 이름이 빈 문자열이면 400을 반환한다")
    void createPublic_실패_이름없음() throws Exception {
        String invalidRequest = "{\"name\":\"\",\"description\":\"설명\"}";

        // when & then: 유효성 검사 실패 → 400
        mockMvc.perform(post("/api/channels/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                // GlobalExceptionHandler의 handleValidationException이 처리
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.name").exists());
    }


    @Test
    @DisplayName("GET /api/channels - userId로 채널 목록을 조회하면 200과 목록을 반환한다")
    void findAll_성공() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        ChannelDto channel = new ChannelDto(
                UUID.randomUUID(), ChannelType.PUBLIC, "공개채널", "설명", List.of(), Instant.now()
        );
        given(channelService.findAllByUserId(userId)).willReturn(List.of(channel));

        // when & then
        mockMvc.perform(get("/api/channels")
                        .param("userId", userId.toString()))  // 쿼리 파라미터 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("공개채널"))
                .andExpect(jsonPath("$[0].type").value("PUBLIC"));
    }
}