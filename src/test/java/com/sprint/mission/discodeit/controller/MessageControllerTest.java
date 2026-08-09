package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MessageController.class)
@ActiveProfiles("test")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;


    @Test
    @DisplayName("GET /api/messages - channelId로 메시지 목록을 조회하면 200과 PageResponse를 반환한다")
    void findAllByChannelId_성공() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        MessageDto message = new MessageDto(
                UUID.randomUUID(), Instant.now(), null,
                "안녕하세요!", channelId, null, List.of()
        );
        PageResponse<MessageDto> pageResponse = new PageResponse<>(
                List.of(message), null, 50, false, null
        );
        given(messageService.findAllByChannelId(any(), any(), any())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                // PageResponse 구조 검증
                .andExpect(jsonPath("$.content[0].content").value("안녕하세요!"))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    @DisplayName("GET /api/messages - 메시지가 없으면 빈 content를 반환한다")
    void findAllByChannelId_빈목록() throws Exception {
        // given
        UUID channelId = UUID.randomUUID();
        PageResponse<MessageDto> emptyPage = new PageResponse<>(
                List.of(), null, 50, false, null
        );
        given(messageService.findAllByChannelId(any(), any(), any())).willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/messages")
                        .param("channelId", channelId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }


    @Test
    @DisplayName("DELETE /api/messages/{messageId} - 삭제 성공 시 204를 반환한다")
    void delete_성공() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                .andExpect(status().isNoContent());
    }
}