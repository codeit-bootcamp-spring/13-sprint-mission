package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private ChannelRepository channelRepository;

    @Test
    @DisplayName("채널 생성 API 요청이 성공하면 데이터베이스에 저장된다")
    void createChannelSuccess() throws Exception {
        // given
        String requestJson = """
                {
                  "name": "integration-channel",
                  "description": "통합 테스트 채널입니다."
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/channels")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("PUBLIC"))
                .andExpect(jsonPath("$.name")
                        .value("integration-channel"))
                .andExpect(jsonPath("$.description")
                        .value("통합 테스트 채널입니다."));

        assertThat(channelRepository.findAll())
                .hasSize(1);

        Channel savedChannel = channelRepository.findAll().get(0);

        assertThat(savedChannel.getType())
                .isEqualTo(ChannelType.PUBLIC);

        assertThat(savedChannel.getName())
                .isEqualTo("integration-channel");

        assertThat(savedChannel.getDescription())
                .isEqualTo("통합 테스트 채널입니다.");
    }

    @Test
    @DisplayName("채널 이름이 비어 있으면 생성 요청에 실패한다")
    void createChannelFailWhenNameIsBlank() throws Exception {
        // given
        String requestJson = """
                {
                  "name": "",
                  "description": "설명입니다."
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/channels")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.details.name")
                        .value("채널 이름은 필수입니다."));

        assertThat(channelRepository.count())
                .isZero();
    }

    @Test
    @DisplayName("채널 단건 조회 API는 저장된 채널 정보를 반환한다")
    void findChannelSuccess() throws Exception {
        // given
        Channel savedChannel = channelRepository.saveAndFlush(
                createChannel(
                        "조회 채널",
                        "조회 테스트용 채널"
                )
        );

        // when & then
        mockMvc.perform(
                        get("/channels/{id}", savedChannel.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedChannel.getId().toString()))
                .andExpect(jsonPath("$.type")
                        .value("PUBLIC"))
                .andExpect(jsonPath("$.name")
                        .value("조회 채널"))
                .andExpect(jsonPath("$.description")
                        .value("조회 테스트용 채널"));
    }

    @Test
    @DisplayName("존재하지 않는 채널을 조회하면 404 응답을 반환한다")
    void findChannelFailWhenNotFound() throws Exception {
        // given
        UUID unknownChannelId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/channels/{id}", unknownChannelId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("ChannelNotFoundException"))
                .andExpect(jsonPath("$.details.channelId")
                        .value(unknownChannelId.toString()));
    }

    @Test
    @DisplayName("채널 수정 API 요청이 성공하면 데이터베이스 정보가 변경된다")
    void updateChannelSuccess() throws Exception {
        // given
        Channel savedChannel = channelRepository.saveAndFlush(
                createChannel(
                        "수정 전 채널",
                        "수정 전 설명"
                )
        );

        String requestJson = """
                {
                  "name": "수정된 채널",
                  "description": "수정된 설명"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/channels/{id}", savedChannel.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedChannel.getId().toString()))
                .andExpect(jsonPath("$.type")
                        .value("PUBLIC"))
                .andExpect(jsonPath("$.name")
                        .value("수정된 채널"))
                .andExpect(jsonPath("$.description")
                        .value("수정된 설명"));

        channelRepository.flush();

        Channel updatedChannel = channelRepository
                .findById(savedChannel.getId())
                .orElseThrow();

        assertThat(updatedChannel.getName())
                .isEqualTo("수정된 채널");

        assertThat(updatedChannel.getDescription())
                .isEqualTo("수정된 설명");

        assertThat(updatedChannel.getType())
                .isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("존재하지 않는 채널을 수정하면 404 응답을 반환한다")
    void updateChannelFailWhenNotFound() throws Exception {
        // given
        UUID unknownChannelId = UUID.randomUUID();

        String requestJson = """
                {
                  "name": "수정된 채널",
                  "description": "수정된 설명"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/channels/{id}", unknownChannelId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.details.channelId")
                        .value(unknownChannelId.toString()));
    }

    @Test
    @DisplayName("채널 삭제 API 요청이 성공하면 데이터베이스에서 제거된다")
    void deleteChannelSuccess() throws Exception {
        // given
        Channel savedChannel = channelRepository.saveAndFlush(
                createChannel(
                        "삭제 채널",
                        "삭제 테스트용 채널"
                )
        );

        UUID channelId = savedChannel.getId();

        // when & then
        mockMvc.perform(
                        delete("/channels/{id}", channelId)
                )
                .andExpect(status().isOk());

        channelRepository.flush();

        assertThat(channelRepository.findById(channelId))
                .isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 채널을 삭제하면 404 응답을 반환한다")
    void deleteChannelFailWhenNotFound() throws Exception {
        // given
        UUID unknownChannelId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/channels/{id}", unknownChannelId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("CHANNEL_NOT_FOUND"));
    }

    @Test
    @DisplayName("채널 목록 조회 API는 저장된 채널 목록을 반환한다")
    void findAllChannelsSuccess() throws Exception {
        // given
        channelRepository.save(
                createChannel(
                        "channel01",
                        "첫 번째 채널"
                )
        );

        channelRepository.save(
                createChannel(
                        "channel02",
                        "두 번째 채널"
                )
        );

        channelRepository.flush();

        // when & then
        mockMvc.perform(
                        get("/channels")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name")
                        .value(hasItem("channel01")))
                .andExpect(jsonPath("$[*].name")
                        .value(hasItem("channel02")));
    }

    private Channel createChannel(
            String name,
            String description
    ) {
        return new Channel(
                ChannelType.PUBLIC,
                name,
                description
        );
    }
}