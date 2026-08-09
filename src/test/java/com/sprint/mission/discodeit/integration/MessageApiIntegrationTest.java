package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.UpdateMessageRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("메시지를 생성하면 DB에 저장되고 201을 반환한다")
    void createMessage_success() throws Exception {
        // given
        User author = saveUser(
                "홍길동",
                "hong12@test.com"
        );

        Channel channel = saveChannel(
                "공지 채널"
        );

        CreateMessageRequest request =
                new CreateMessageRequest(
                        author.getId(),
                        channel.getId(),
                        "안녕하세요."
                );

        MockMultipartFile requestPart =
                createMessageRequestPart(request);

        long countBefore =
                messageRepository.count();

        // when & then
        mockMvc.perform(
                        multipart("/api/messages")
                                .file(requestPart)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("안녕하세요.")
                )
                .andExpect(
                        jsonPath("$.channelId")
                                .value(channel.getId().toString())
                )
                .andExpect(
                        jsonPath("$.author.id")
                                .value(author.getId().toString())
                )
                .andExpect(
                        jsonPath("$.author.username")
                                .value("홍길동")
                )
                .andExpect(
                        jsonPath("$.attachments")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.attachments")
                                .isEmpty()
                );
        assertThat(messageRepository.count())
                .isEqualTo(countBefore + 1);

        Message savedMessage =
                messageRepository.findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertThat(savedMessage.getContent())
                .isEqualTo("안녕하세요.");

        assertThat(savedMessage.getChannel().getId())
                .isEqualTo(channel.getId());

        assertThat(savedMessage.getAuthor().getId())
                .isEqualTo(author.getId());
    }

    @Test
    @DisplayName("메시지를 수정하면 응답과 DB에 반영된다")
    void updateMessage_success() throws Exception {
        // given
        User author = saveUser(
                "김철수",
                "kim2@test.com"
        );

        Channel channel = saveChannel(
                "소통 채널"
        );

        UUID messageId = createMessageThroughApi(
                author.getId(),
                channel.getId(),
                "안녕하세요."
        );

        UpdateMessageRequest request =
                new UpdateMessageRequest(
                        "잘 부탁드립니다."
                );

        long countBefore =
                messageRepository.count();

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                messageId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(messageId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("잘 부탁드립니다.")
                )
                .andExpect(
                        jsonPath("$.channelId")
                                .value(channel.getId().toString())
                );

        entityManager.flush();
        entityManager.clear();
        assertThat(messageRepository.count())
                .isEqualTo(countBefore);

        Message updatedMessage =
                messageRepository.findById(messageId)
                        .orElseThrow();

        assertThat(updatedMessage.getContent())
                .isEqualTo("잘 부탁드립니다.");
    }

    @Test
    @DisplayName("메시지를 삭제하면 204를 반환하고 DB에서 제거된다")
    void deleteMessage_success() throws Exception {
        // given
        User author = saveUser(
                "반달곰",
                "delete-message@test.com"
        );

        Channel channel = saveChannel(
                "지리산"
        );

        UUID messageId = createMessageThroughApi(
                author.getId(),
                channel.getId(),
                "반달곰 지리산"
        );

        long countBefore =
                messageRepository.count();

        // when & then
        mockMvc.perform(
                        delete(
                                "/api/messages/{messageId}",
                                messageId
                        )
                )
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(messageRepository.count())
                .isEqualTo(countBefore - 1);

        assertThat(
                messageRepository.findById(messageId)
        ).isEmpty();
    }

    @Test
    @DisplayName("특정 채널의 메시지를 최근순으로 2개 조회한다")
    void getMessages_success_pagingAndSorting() throws Exception {
        // given
        User author = saveUser(
                "아기곰",
                "bear7@test.com"
        );

        Channel channel = saveChannel(
                "아기곰 채널"
        );

        createMessageThroughApi(
                author.getId(),
                channel.getId(),
                "안녕하세요."
        );

        createMessageThroughApi(
                author.getId(),
                channel.getId(),
                "아기곰 입니다."
        );

        createMessageThroughApi(
                author.getId(),
                channel.getId(),
                "잘 부탁드려요."
        );

        entityManager.flush();
        entityManager.clear();

        // when & then
        mockMvc.perform(
                        get("/api/messages")
                                .param(
                                        "channelId",
                                        channel.getId().toString()
                                )
                                .param("page", "0")
                                .param("size", "2")
                                .param(
                                        "sort",
                                        "createdAt,desc"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.content[0].content")
                                .value("잘 부탁드려요.")
                )
                .andExpect(
                        jsonPath("$.content[1].content")
                                .value("아기곰 입니다.")
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.hasNext")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.nextCursor")
                                .exists()
                );
    }

    @Test
    @DisplayName("메시지 내용이 공백이면 생성되지 않고 400을 반환한다")
    void createMessage_fail_contentBlank() throws Exception {
        // given
        User author = saveUser(
                "흑곰",
                "blackBear@test.com"
        );

        Channel channel = saveChannel(
                "흑곰 채널"
        );

        CreateMessageRequest request =
                new CreateMessageRequest(
                        author.getId(),
                        channel.getId(),
                        " "
                );

        MockMultipartFile requestPart =
                createMessageRequestPart(request);
        long countBefore =
                messageRepository.count();

        // when & then
        mockMvc.perform(
                        multipart("/api/messages")
                                .file(requestPart)
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
                        jsonPath("$.details.content")
                                .exists()
                );
        assertThat(messageRepository.count())
                .isEqualTo(countBefore);
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 수정하면 404를 반환한다")
    void updateMessage_fail_messageNotFound() throws Exception {
        // given
        UUID unknownMessageId =
                UUID.randomUUID();

        UpdateMessageRequest request =
                new UpdateMessageRequest(
                        "반갑습니다."
                );

        long countBefore =
                messageRepository.count();

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/messages/{messageId}",
                                unknownMessageId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(
                        jsonPath("$.code")
                                .value("MESSAGE_NOT_FOUND")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.details.messageId")
                                .value(unknownMessageId.toString())
                );
        assertThat(messageRepository.count())
                .isEqualTo(countBefore);
    }

    @Test
    @DisplayName("존재하지 않는 채널로 메시지를 생성하면 404를 반환한다")
    void createMessage_fail_channelNotFound() throws Exception {
        // given
        User author = saveUser(
                "채널검증",
                "channel-validation@test.com"
        );

        UUID unknownChannelId =
                UUID.randomUUID();

        CreateMessageRequest request =
                new CreateMessageRequest(
                        author.getId(),
                        unknownChannelId,
                        "채널 검증하는 중"
                );

        MockMultipartFile requestPart =
                createMessageRequestPart(request);
        long countBefore =
                messageRepository.count();

        // when & then
        mockMvc.perform(
                        multipart("/api/messages")
                                .file(requestPart)
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
        assertThat(messageRepository.count())
                .isEqualTo(countBefore);
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
    private Channel saveChannel(
            String name
    ) {
        Channel channel = new Channel(
                name,
                name + "설명",
                ChannelType.PUBLIC
        );

        return channelRepository.saveAndFlush(channel);
    }

    private MockMultipartFile createMessageRequestPart(
            CreateMessageRequest request
    ) throws Exception {
        return new MockMultipartFile(
                "messageCreateRequest",
                "messageCreateRequest.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private UUID createMessageThroughApi(
            UUID authorId,
            UUID channelId,
            String content
    ) throws Exception {
        CreateMessageRequest request =
                new CreateMessageRequest(
                        authorId,
                        channelId,
                        content
                );

        MockMultipartFile requestPart =
                createMessageRequestPart(request);

        MvcResult result = mockMvc.perform(
                        multipart("/api/messages")
                                .file(requestPart)
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