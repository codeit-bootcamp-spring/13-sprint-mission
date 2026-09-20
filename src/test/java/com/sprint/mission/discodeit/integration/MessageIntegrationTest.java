package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    @DisplayName("메시지 생성 API 요청이 성공하면 데이터베이스에 저장된다")
    void createMessageSuccess() throws Exception {
        // given
        User author = saveUser(
                "message-user",
                "message-user@example.com"
        );

        Channel channel = saveChannel(
                "message-channel",
                "메시지 통합 테스트 채널"
        );

        String requestJson = """
                {
                  "content": "통합 테스트 메시지입니다.",
                  "userId": "%s",
                  "channelId": "%s"
                }
                """.formatted(
                author.getId(),
                channel.getId()
        );

        // when & then
        mockMvc.perform(
                        post("/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content")
                        .value("통합 테스트 메시지입니다."))
                .andExpect(jsonPath("$.channelId")
                        .value(channel.getId().toString()))
                .andExpect(jsonPath("$.author.id")
                        .value(author.getId().toString()))
                .andExpect(jsonPath("$.author.username")
                        .value("message-user"))
                .andExpect(jsonPath("$.attachments").isArray());

        assertThat(messageRepository.count()).isEqualTo(1);

        Message savedMessage = messageRepository.findAll().get(0);

        assertThat(savedMessage.getContent())
                .isEqualTo("통합 테스트 메시지입니다.");

        assertThat(savedMessage.getChannel().getId())
                .isEqualTo(channel.getId());

        assertThat(savedMessage.getAuthor().getId())
                .isEqualTo(author.getId());
    }

    @Test
    @DisplayName("메시지 내용이 비어 있으면 생성 요청에 실패한다")
    void createMessageFailWhenContentIsBlank() throws Exception {
        // given
        User author = saveUser(
                "blank-user",
                "blank-user@example.com"
        );

        Channel channel = saveChannel(
                "blank-channel",
                "Validation 테스트 채널"
        );

        String requestJson = """
                {
                  "content": "",
                  "userId": "%s",
                  "channelId": "%s"
                }
                """.formatted(
                author.getId(),
                channel.getId()
        );

        // when & then
        mockMvc.perform(
                        post("/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MethodArgumentNotValidException"))
                .andExpect(jsonPath("$.details.content")
                        .value("메시지 내용은 필수입니다."));

        assertThat(messageRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 메시지를 생성하면 404 응답을 반환한다")
    void createMessageFailWhenUserNotFound() throws Exception {
        // given
        UUID unknownUserId = UUID.randomUUID();

        Channel channel = saveChannel(
                "user-not-found-channel",
                "사용자 조회 실패 테스트"
        );

        String requestJson = """
                {
                  "content": "메시지 내용",
                  "userId": "%s",
                  "channelId": "%s"
                }
                """.formatted(
                unknownUserId,
                channel.getId()
        );

        // when & then
        mockMvc.perform(
                        post("/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("UserNotFoundException"))
                .andExpect(jsonPath("$.details.userId")
                        .value(unknownUserId.toString()));

        assertThat(messageRepository.count()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 채널로 메시지를 생성하면 404 응답을 반환한다")
    void createMessageFailWhenChannelNotFound() throws Exception {
        // given
        User author = saveUser(
                "channel-not-found-user",
                "channel-not-found@example.com"
        );

        UUID unknownChannelId = UUID.randomUUID();

        String requestJson = """
                {
                  "content": "메시지 내용",
                  "userId": "%s",
                  "channelId": "%s"
                }
                """.formatted(
                author.getId(),
                unknownChannelId
        );

        // when & then
        mockMvc.perform(
                        post("/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("CHANNEL_NOT_FOUND"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("ChannelNotFoundException"))
                .andExpect(jsonPath("$.details.channelId")
                        .value(unknownChannelId.toString()));

        assertThat(messageRepository.count()).isZero();
    }

    @Test
    @DisplayName("메시지 단건 조회 API는 저장된 메시지를 반환한다")
    void findMessageSuccess() throws Exception {
        // given
        User author = saveUser(
                "find-user",
                "find-user@example.com"
        );

        Channel channel = saveChannel(
                "find-channel",
                "메시지 조회 채널"
        );

        Message message = saveMessage(
                "조회할 메시지",
                channel,
                author
        );

        // when & then
        mockMvc.perform(
                        get("/messages/{id}", message.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(message.getId().toString()))
                .andExpect(jsonPath("$.content")
                        .value("조회할 메시지"))
                .andExpect(jsonPath("$.channelId")
                        .value(channel.getId().toString()))
                .andExpect(jsonPath("$.author.id")
                        .value(author.getId().toString()));
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 조회하면 404 응답을 반환한다")
    void findMessageFailWhenNotFound() throws Exception {
        // given
        UUID unknownMessageId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/messages/{id}", unknownMessageId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("MESSAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.exceptionType")
                        .value("MessageNotFoundException"))
                .andExpect(jsonPath("$.details.messageId")
                        .value(unknownMessageId.toString()));
    }

    @Test
    @DisplayName("채널별 메시지 목록 조회 API는 해당 채널의 메시지만 반환한다")
    void findMessagesByChannelIdSuccess() throws Exception {
        // given
        User author = saveUser(
                "list-user",
                "list-user@example.com"
        );

        Channel channel = saveChannel(
                "list-channel",
                "메시지 목록 조회 채널"
        );

        Channel otherChannel = saveChannel(
                "other-channel",
                "다른 채널"
        );

        saveMessage(
                "첫 번째 메시지",
                channel,
                author
        );

        saveMessage(
                "두 번째 메시지",
                channel,
                author
        );

        saveMessage(
                "다른 채널 메시지",
                otherChannel,
                author
        );

        // when & then
        mockMvc.perform(
                        get("/messages")
                                .param(
                                        "channelId",
                                        channel.getId().toString()
                                )
                                .param("page", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").isEmpty());
    }

    @Test
    @DisplayName("메시지 수정 API 요청이 성공하면 데이터베이스 정보가 변경된다")
    void updateMessageSuccess() throws Exception {
        // given
        User author = saveUser(
                "update-user",
                "update-user@example.com"
        );

        Channel channel = saveChannel(
                "update-channel",
                "메시지 수정 채널"
        );

        Message message = saveMessage(
                "수정 전 메시지",
                channel,
                author
        );

        String requestJson = """
                {
                  "content": "수정된 메시지"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/messages/{id}", message.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(message.getId().toString()))
                .andExpect(jsonPath("$.content")
                        .value("수정된 메시지"));

        messageRepository.flush();

        Message updatedMessage = messageRepository
                .findById(message.getId())
                .orElseThrow();

        assertThat(updatedMessage.getContent())
                .isEqualTo("수정된 메시지");
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 수정하면 404 응답을 반환한다")
    void updateMessageFailWhenNotFound() throws Exception {
        // given
        UUID unknownMessageId = UUID.randomUUID();

        String requestJson = """
                {
                  "content": "수정된 메시지"
                }
                """;

        // when & then
        mockMvc.perform(
                        put("/messages/{id}", unknownMessageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("MESSAGE_NOT_FOUND"))
                .andExpect(jsonPath("$.details.messageId")
                        .value(unknownMessageId.toString()));
    }

    @Test
    @DisplayName("메시지 삭제 API 요청이 성공하면 데이터베이스에서 제거된다")
    void deleteMessageSuccess() throws Exception {
        // given
        User author = saveUser(
                "delete-message-user",
                "delete-message-user@example.com"
        );

        Channel channel = saveChannel(
                "delete-message-channel",
                "메시지 삭제 채널"
        );

        Message message = saveMessage(
                "삭제할 메시지",
                channel,
                author
        );

        UUID messageId = message.getId();

        // when & then
        mockMvc.perform(
                        delete("/messages/{id}", messageId)
                )
                .andExpect(status().isOk());

        messageRepository.flush();

        assertThat(messageRepository.findById(messageId))
                .isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 삭제하면 404 응답을 반환한다")
    void deleteMessageFailWhenNotFound() throws Exception {
        // given
        UUID unknownMessageId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        delete("/messages/{id}", unknownMessageId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("MESSAGE_NOT_FOUND"));
    }

    private User saveUser(
            String username,
            String email
    ) {
        User user = new User(
                username,
                email,
                "password123",
                null
        );

        return userRepository.saveAndFlush(user);
    }

    private Channel saveChannel(
            String name,
            String description
    ) {
        Channel channel = new Channel(
                ChannelType.PUBLIC,
                name,
                description
        );

        return channelRepository.saveAndFlush(channel);
    }

    private Message saveMessage(
            String content,
            Channel channel,
            User author
    ) {
        Message message = new Message(
                content,
                channel,
                author,
                List.of()
        );

        return messageRepository.saveAndFlush(message);
    }
}