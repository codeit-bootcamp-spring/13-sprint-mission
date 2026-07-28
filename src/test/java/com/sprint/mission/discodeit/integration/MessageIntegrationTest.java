package com.sprint.mission.discodeit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Nested
    @DisplayName("메시지 생성 API")
    class CreateMessage {

        @Test
        @DisplayName("정상적인 요청이면 메시지를 생성하고 201을 반환")
        void 메시지_생성_성공() throws Exception {
            // given
            User author = saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Channel channel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            MessageCreateRequest request = new MessageCreateRequest(
                    "안녕하세요",
                    channel.getId(),
                    author.getId()
            );

            MockMultipartFile requestPart = createMessageRequestPart(request);

            // when & then
            mockMvc.perform(multipart("/api/messages")
                            .file(requestPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.content")
                            .value("안녕하세요"));

            List<Message> messages = messageRepository.findAll();

            assertThat(messages).hasSize(1);

            Message savedMessage = messages.get(0);

            assertThat(savedMessage.getContent())
                    .isEqualTo("안녕하세요");
            assertThat(savedMessage.getChannel().getId())
                    .isEqualTo(channel.getId());
            assertThat(savedMessage.getAuthor().getId())
                    .isEqualTo(author.getId());
        }

        @Test
        @DisplayName("존재하지 않는 채널에 메시지를 생성하면 404를 반환")
        void 메시지_생성_실패_채널_없음() throws Exception {
            // given
            User author = saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            UUID unknownChannelId = UUID.randomUUID();

            MessageCreateRequest request = new MessageCreateRequest(
                    "안녕하세요",
                    unknownChannelId,
                    author.getId()
            );

            MockMultipartFile requestPart = createMessageRequestPart(request);

            // when & then
            mockMvc.perform(multipart("/api/messages")
                            .file(requestPart)
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("CHANNEL_NOT_FOUND"));

            assertThat(messageRepository.findAll())
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("메시지 수정 API")
    class UpdateMessage {

        @Test
        @DisplayName("존재하는 메시지의 내용을 수정하고 200을 반환")
        void 메시지_수정_성공() throws Exception {
            // given
            User author = saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Channel channel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            Message savedMessage = saveMessage(
                    "기존 메시지",
                    channel,
                    author
            );

            MessageUpdateRequest request = new MessageUpdateRequest(
                    "수정된 메시지"
            );

            // when & then
            mockMvc.perform(patch("/api/messages/{messageId}", savedMessage.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(savedMessage.getId().toString()))
                    .andExpect(jsonPath("$.content")
                            .value("수정된 메시지"));

            Message updatedMessage = messageRepository
                    .findById(savedMessage.getId())
                    .orElseThrow();

            assertThat(updatedMessage.getContent())
                    .isEqualTo("수정된 메시지");
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 수정하면 404를 반환")
        void 메시지_수정_실패_메시지_없음() throws Exception {
            // given
            UUID unknownMessageId = UUID.randomUUID();

            MessageUpdateRequest request = new MessageUpdateRequest(
                    "수정된 메시지"
            );

            // when & then
            mockMvc.perform(patch("/api/messages/{messageId}", unknownMessageId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("MESSAGE_NOT_FOUND"));

            assertThat(messageRepository.findById(unknownMessageId))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("메시지 삭제 API")
    class DeleteMessage {

        @Test
        @DisplayName("존재하는 메시지를 삭제하고 204를 반환")
        void 메시지_삭제_성공() throws Exception {
            // given
            User author = saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Channel channel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            Message savedMessage = saveMessage(
                    "삭제할 메시지",
                    channel,
                    author
            );

            UUID messageId = savedMessage.getId();

            // when & then
            mockMvc.perform(delete("/api/messages/{messageId}", messageId))
                    .andExpect(status().isNoContent());

            assertThat(messageRepository.findById(messageId))
                    .isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 삭제하면 404를 반환")
        void 메시지_삭제_실패_메시지_없음() throws Exception {
            // given
            UUID unknownMessageId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/messages/{messageId}", unknownMessageId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code")
                            .value("MESSAGE_NOT_FOUND"));

            assertThat(messageRepository.findById(unknownMessageId))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("채널별 메시지 목록 조회 API")
    class FindMessagesByChannel {

        @Test
        @DisplayName("해당 채널의 메시지 목록을 반환")
        void 메시지_목록_조회_성공() throws Exception {
            // given
            User author = saveUser(
                    "user1",
                    "user1@test.com",
                    "password1"
            );

            Channel targetChannel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            Channel otherChannel = savePublicChannel(
                    "자유",
                    "자유 대화 채널"
            );

            saveMessage(
                    "첫 번째 메시지",
                    targetChannel,
                    author
            );

            saveMessage(
                    "두 번째 메시지",
                    targetChannel,
                    author
            );

            saveMessage(
                    "다른 채널 메시지",
                    otherChannel,
                    author
            );

            // when & then
            mockMvc.perform(get("/api/messages")
                                    .param(
                                            "channelId",
                                            targetChannel.getId().toString()
                                    )
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param(
                                            "sort",
                                            "createdAt,asc"
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content.length()")
                            .value(2))
                    .andExpect(jsonPath("$.content[0].content")
                            .value("첫 번째 메시지"))
                    .andExpect(jsonPath("$.content[1].content")
                            .value("두 번째 메시지"))
                    .andExpect(jsonPath("$.size")
                            .value(10))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false))
                    .andExpect(jsonPath("$.totalElements")
                            .isEmpty());
        }

        @Test
        @DisplayName("채널에 메시지가 없으면 빈 목록을 반환")
        void 메시지_목록_조회_결과_없음() throws Exception {
            // given
            Channel channel = savePublicChannel(
                    "공지",
                    "전체 공지 채널"
            );

            // when & then
            mockMvc.perform(get("/api/messages")
                                    .param(
                                            "channelId",
                                            channel.getId().toString()
                                    )
                                    .param("page", "0")
                                    .param("size", "10")
                                    .param(
                                            "sort",
                                            "createdAt,desc"
                                    )
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content")
                            .isArray())
                    .andExpect(jsonPath("$.content")
                            .isEmpty())
                    .andExpect(jsonPath("$.size")
                            .value(10))
                    .andExpect(jsonPath("$.hasNext")
                            .value(false))
                    .andExpect(jsonPath("$.totalElements")
                            .isEmpty());

            assertThat(messageRepository.findAllByChannelId(channel.getId()))
                    .isEmpty();
        }
    }

    private User saveUser(String username, String email, String password) {
        User user = new User(
                username,
                email,
                password,
                null
        );

        return userRepository.saveAndFlush(user);
    }

    private Channel savePublicChannel(String name, String description) {
        Channel channel = new Channel(
                ChannelType.PUBLIC,
                name,
                description
        );

        return channelRepository.saveAndFlush(channel);
    }

    private Message saveMessage(String content, Channel channel, User author) {
        Message message = new Message(
                content,
                channel,
                author,
                List.of()
        );

        return messageRepository.saveAndFlush(message);
    }

    private MockMultipartFile createMessageRequestPart(MessageCreateRequest request) throws Exception {
        return new MockMultipartFile(
                "messageCreateRequest",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

}
