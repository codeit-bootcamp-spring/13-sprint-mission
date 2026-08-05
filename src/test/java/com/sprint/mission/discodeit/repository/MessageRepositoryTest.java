package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Nested
    @DisplayName("채널별 메시지 페이징 조회")
    class FindAllByChannelIdPageable {

        @Test
        @DisplayName("해당 채널의 메시지를 조회")
        void get_success() {
            // given
            User user = new User(
                    "user1",
                    "user1@test.com",
                    "password1",
                    null
            );
            userRepository.saveAndFlush(user);

            Channel targetChannel = new Channel(
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널"
            );
            Channel otherChannel = new Channel(
                    ChannelType.PUBLIC,
                    "자유",
                    "자유 대화 채널"
            );
            channelRepository.saveAllAndFlush(
                    List.of(targetChannel, otherChannel)
            );

            Message firstMessage = new Message(
                    "첫 번째 메시지",
                    targetChannel,
                    user,
                    List.of()
            );

            Message secondMessage = new Message(
                    "두 번째 메시지",
                    targetChannel,
                    user,
                    List.of()
            );

            Message otherChannelMessage = new Message(
                    "다른 채널 메시지",
                    otherChannel,
                    user,
                    List.of()
            );
            messageRepository.saveAllAndFlush(
                    List.of(
                            firstMessage,
                            secondMessage,
                            otherChannelMessage
                    )
            );

            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by("createdAt").ascending()
            );

            // when
            Slice<Message> result =
                    messageRepository.findAllByChannelId(
                            targetChannel.getId(),
                            pageable
                    );

            // then
            assertThat(result.getContent())
                    .hasSize(2);

            assertThat(result.getContent())
                    .extracting(Message::getContent)
                    .containsExactly("첫 번째 메시지", "두 번째 메시지");
        }

        @Test
        @DisplayName("해당 채널에 메시지가 없으면 빈 Slice를 반환")
        void get_fail() {
            // given
            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널"
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Slice<Message> result =
                    messageRepository.findAllByChannelId(
                            channel.getId(),
                            pageable
                    );

            // then
            assertThat(result.getContent())
                    .isEmpty();
            assertThat(result.hasNext())
                    .isFalse();
        }

    }

    @Nested
    @DisplayName("작성자별 메시지 조회")
    class FindAllByAuthorId {

        @Test
        @DisplayName("해당 사용자가 작성한 메시지를 조회")
        void get_success() {
            // given
            User targetUser = new User(
                    "user1",
                    "user1@test.com",
                    "password1",
                    null
            );
            User otherUser = new User(
                    "user2",
                    "user2@test.com",
                    "password2",
                    null
            );
            userRepository.saveAllAndFlush(
                    List.of(
                            targetUser,
                            otherUser
                    )
            );

            Channel channel = new Channel(
                    ChannelType.PUBLIC,
                    "공지",
                    "전체 공지 채널"
            );
            channelRepository.saveAndFlush(channel);

            Message firstMessage = new Message(
                    "첫 번째 메시지",
                    channel,
                    targetUser,
                    List.of()
            );

            Message secondMessage = new Message(
                    "두 번째 메시지",
                    channel,
                    targetUser,
                    List.of()
            );

            Message otherUserMessage = new Message(
                    "다른 사용자 메시지",
                    channel,
                    otherUser,
                    List.of()
            );
            messageRepository.saveAllAndFlush(
                    List.of(
                            firstMessage,
                            secondMessage,
                            otherUserMessage
                    )
            );

            // when
            List<Message> result =
                    messageRepository.findAllByAuthorId(
                            targetUser.getId()
                    );

            // then
            assertThat(result)
                    .hasSize(2);

            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactlyInAnyOrder(
                            "첫 번째 메시지",
                            "두 번째 메시지"
                    );
        }

        @Test
        @DisplayName("해당 사용자가 작성한 메시지가 없으면 빈 목록을 반환")
        void get_fail() {
            // given
            User user = new User(
                    "user1",
                    "user1@test.com",
                    "password1",
                    null
            );
            userRepository.saveAndFlush(user);

            // when
            List<Message> result = messageRepository.findAllByAuthorId(user.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

}