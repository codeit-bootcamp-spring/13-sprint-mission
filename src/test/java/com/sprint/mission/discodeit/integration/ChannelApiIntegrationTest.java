package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.UpdateChannelRequset;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReadStatusRepository readStatusRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("공개 채널을 생성하면 DB에 저장되고 201을 반환한다")
    void createPublicChannel_success() throws Exception {
        // given
        CreatePublicChannelRequest request =
                new CreatePublicChannelRequest(
                        "공지 채널",
                        "전체 공지 채널입니다."
                );

        // when & then
        mockMvc.perform(
                        post("/api/channels/public")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("PUBLIC")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("공지 채널")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("전체 공지 채널입니다.")
                );

        List<Channel> channels =
                channelRepository.findAll();

        assertThat(channels)
                .hasSize(1);

        Channel savedChannel = channels.get(0);

        assertThat(savedChannel.getType())
                .isEqualTo(ChannelType.PUBLIC);

        assertThat(savedChannel.getName())
                .isEqualTo("공지 채널");

        assertThat(savedChannel.getDescription())
                .isEqualTo("전체 공지 채널입니다.");
    }

    @Test
    @DisplayName("참여자를 지정해 비공개 채널을 생성할 수 있다")
    void createPrivateChannel_success() throws Exception {
        // given
        User firstUser = saveUser(
                "홍길동",
                "hong12@test.com"
        );

        User secondUser = saveUser(
                "김철수",
                "kim2@test.com"
        );

        CreatePrivateChannelRequest request =
                new CreatePrivateChannelRequest(
                        List.of(
                                firstUser.getId(),
                                secondUser.getId()
                        )
                );

        // when & then
        MvcResult result = mockMvc.perform(
                        post("/api/channels/private")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("PRIVATE")
                )
                .andExpect(
                        jsonPath("$.participants.length()")
                                .value(2)
                )
                .andReturn();

        UUID channelId =
                extractId(result);

        entityManager.flush();
        entityManager.clear();

        Channel savedChannel =
                channelRepository.findById(channelId)
                        .orElseThrow();

        assertThat(savedChannel.getType())
                .isEqualTo(ChannelType.PRIVATE);

        assertThat(
                readStatusRepository.findAllByChannelId(channelId)
        ).hasSize(2);
    }

    @Test
    @DisplayName("공개 채널을 수정하면 응답과 DB에 반영된다")
    void updatePublicChannel_success() throws Exception {
        // given
        UUID channelId = createPublicChannelThroughApi(
                "공지 채널",
                "공지 채널입니다."
        );

        UpdateChannelRequset request =
                new UpdateChannelRequset(
                        "대화 채널",
                        "소통 채널입니다."
                );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                channelId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(channelId.toString())
                )
                .andExpect(
                        jsonPath("$.type")
                                .value("PUBLIC")
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("대화 채널")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("소통 채널입니다.")
                );

        entityManager.flush();
        entityManager.clear();

        Channel updatedChannel =
                channelRepository.findById(channelId)
                        .orElseThrow();

        assertThat(updatedChannel.getName())
                .isEqualTo("대화 채널");

        assertThat(updatedChannel.getDescription())
                .isEqualTo("소통 채널입니다.");
    }

    @Test
    @DisplayName("공개 채널을 삭제하면 204를 반환하고 DB에서 제거된다")
    void deleteChannel_success() throws Exception {
        // given
        UUID channelId = createPublicChannelThroughApi(
                "삭제 채널",
                "삭제될 채널입니다."
        );

        // when & then
        mockMvc.perform(
                        delete(
                                "/api/channels/{channelId}",
                                channelId
                        )
                )
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        assertThat(
                channelRepository.findById(channelId)
        ).isEmpty();
    }

    @Test
    @DisplayName("공개 채널 이름이 공백이면 생성되지 않고 400을 반환한다")
    void createPublicChannel_fail_nameBlank() throws Exception {
        // given
        CreatePublicChannelRequest request =
                new CreatePublicChannelRequest(
                        " ",
                        "채널 오류"
                );

        // when & then
        mockMvc.perform(
                        post("/api/channels/public")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_REQUEST")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.details.name")
                                .value("채널 이름은 필수입니다.")
                );

        assertThat(channelRepository.count())
                .isZero();
    }

    @Test
    @DisplayName("존재하지 않는 채널을 수정하면 404를 반환한다")
    void updateChannel_fail_channelNotFound() throws Exception {
        // given
        UUID unknownChannelId =
                UUID.randomUUID();

        UpdateChannelRequset request =
                new UpdateChannelRequset(
                        "일반 채널",
                        "일반 채널입니다."
                );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                unknownChannelId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("CHANNEL_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.details.channelId")
                                .value(unknownChannelId.toString())
                );
    }

    @Test
    @DisplayName("비공개 채널은 수정할 수 없다")
    void updatePrivateChannel_fail() throws Exception {
        // given
        User user = saveUser(
                "비공개참여자",
                "private@test.com"
        );

        CreatePrivateChannelRequest createRequest =
                new CreatePrivateChannelRequest(
                        List.of(user.getId())
                );

        MvcResult createResult = mockMvc.perform(
                        post("/api/channels/private")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andReturn();

        UUID privateChannelId =
                extractId(createResult);

        UpdateChannelRequset updateRequest =
                new UpdateChannelRequset(
                        "대화 채널",
                        "소통 채널입니다."
                );

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/channels/{channelId}",
                                privateChannelId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("PRIVATE_CHANNEL_UPDATE")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                );
    }
    private User saveUser(
            String username,
            String email
    ) {
        User user = new User(
                username,
                email,
                "12345"
        );

        return userRepository.saveAndFlush(user);
    }
    private UUID createPublicChannelThroughApi(
            String name,
            String description
    ) throws Exception {
        CreatePublicChannelRequest request =
                new CreatePublicChannelRequest(
                        name,
                        description
                );

        MvcResult result = mockMvc.perform(
                        post("/api/channels/public")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }
    private UUID extractId(
            MvcResult result
    ) throws Exception {
        String responseBody =
                result.getResponse().getContentAsString();

        JsonNode responseJson =
                objectMapper.readTree(responseBody);

        return UUID.fromString(
                responseJson.get("id").asText()
        );
    }
}