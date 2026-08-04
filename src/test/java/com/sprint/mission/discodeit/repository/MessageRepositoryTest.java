package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MessageRepository 슬라이스 테스트")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    private User author1;
    private User author2;

    private Channel channel1;
    private Channel channel2;

    private final Instant firstTime =
            Instant.parse("2026-07-28T01:00:00Z");

    private final Instant secondTime =
            Instant.parse("2026-07-28T02:00:00Z");

    private final Instant thirdTime =
            Instant.parse("2026-07-28T03:00:00Z");

    @BeforeEach
    void setUp() {
        author1 = userRepository.save(
                new User(
                        "author1",
                        "author1@test.com",
                        "password"
                )
        );

        author2 = userRepository.save(
                new User(
                        "author2",
                        "author2@test.com",
                        "password"
                )
        );

        channel1 = channelRepository.save(
                new Channel(
                        ChannelType.PUBLIC,
                        "채널 1",
                        "첫 번째 테스트 채널"
                )
        );

        channel2 = channelRepository.save(
                new Channel(
                        ChannelType.PUBLIC,
                        "채널 2",
                        "두 번째 테스트 채널"
                )
        );

        userRepository.flush();
        channelRepository.flush();
    }

    @Nested
    @DisplayName("채널별 메시지 조회")
    class FindAllByChannelId {

        @Test
        @DisplayName("특정 채널의 메시지만 조회한다")
        void success() {
            // given
            Message channel1Message1 =
                    createMessage(
                            "채널 1 메시지 1",
                            author1,
                            channel1,
                            firstTime
                    );

            Message channel1Message2 =
                    createMessage(
                            "채널 1 메시지 2",
                            author2,
                            channel1,
                            secondTime
                    );

            Message channel2Message =
                    createMessage(
                            "채널 2 메시지",
                            author1,
                            channel2,
                            thirdTime
                    );

            messageRepository.saveAll(
                    List.of(
                            channel1Message1,
                            channel1Message2,
                            channel2Message
                    )
            );

            messageRepository.flush();

            // when
            List<Message> result =
                    messageRepository.findAllByChannel_Id(
                            channel1.getId()
                    );

            // then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactlyInAnyOrder(
                            "채널 1 메시지 1",
                            "채널 1 메시지 2"
                    );

            assertThat(result)
                    .extracting(Message::getChannelId)
                    .containsOnly(channel1.getId());
        }

        @Test
        @DisplayName("채널에 메시지가 없으면 빈 목록을 반환한다")
        void empty() {
            // given
            createAndSaveMessage(
                    "다른 채널 메시지",
                    author1,
                    channel2,
                    firstTime
            );

            // when
            List<Message> result =
                    messageRepository.findAllByChannel_Id(
                            channel1.getId()
                    );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("작성자별 메시지 조회")
    class FindAllByAuthorId {

        @Test
        @DisplayName("특정 작성자가 작성한 메시지만 조회한다")
        void success() {
            // given
            createAndSaveMessage(
                    "작성자 1 메시지 1",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "작성자 1 메시지 2",
                    author1,
                    channel2,
                    secondTime
            );

            createAndSaveMessage(
                    "작성자 2 메시지",
                    author2,
                    channel1,
                    thirdTime
            );

            // when
            List<Message> result =
                    messageRepository.findAllByAuthor_Id(
                            author1.getId()
                    );

            // then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactlyInAnyOrder(
                            "작성자 1 메시지 1",
                            "작성자 1 메시지 2"
                    );

            assertThat(result)
                    .extracting(Message::getAuthorId)
                    .containsOnly(author1.getId());
        }

        @Test
        @DisplayName("작성한 메시지가 없으면 빈 목록을 반환한다")
        void empty() {
            // given
            createAndSaveMessage(
                    "작성자 2 메시지",
                    author2,
                    channel1,
                    firstTime
            );

            // when
            List<Message> result =
                    messageRepository.findAllByAuthor_Id(
                            author1.getId()
                    );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("첫 번째 커서 페이지 조회")
    class FindFirstCursorPage {

        @Test
        @DisplayName("채널 메시지를 생성일 기준 최신순으로 조회한다")
        void success() {
            // given
            createAndSaveMessage(
                    "첫 번째 메시지",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "두 번째 메시지",
                    author1,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "세 번째 메시지",
                    author1,
                    channel1,
                    thirdTime
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Message> result =
                    messageRepository
                            .findByChannel_IdOrderByCreatedAtDesc(
                                    channel1.getId(),
                                    pageable
                            );

            // then
            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactly(
                            "세 번째 메시지",
                            "두 번째 메시지",
                            "첫 번째 메시지"
                    );

            assertThat(result)
                    .extracting(Message::getCreatedAt)
                    .containsExactly(
                            thirdTime,
                            secondTime,
                            firstTime
                    );
        }

        @Test
        @DisplayName("요청한 페이지 크기만큼 메시지를 조회한다")
        void paging() {
            // given
            createAndSaveMessage(
                    "첫 번째 메시지",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "두 번째 메시지",
                    author1,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "세 번째 메시지",
                    author1,
                    channel1,
                    thirdTime
            );

            Pageable pageable = PageRequest.of(0, 2);

            // when
            List<Message> result =
                    messageRepository
                            .findByChannel_IdOrderByCreatedAtDesc(
                                    channel1.getId(),
                                    pageable
                            );

            // then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactly(
                            "세 번째 메시지",
                            "두 번째 메시지"
                    );
        }
    }

    @Nested
    @DisplayName("다음 커서 페이지 조회")
    class FindNextCursorPage {

        @Test
        @DisplayName("커서보다 오래된 메시지만 최신순으로 조회한다")
        void success() {
            // given
            createAndSaveMessage(
                    "첫 번째 메시지",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "두 번째 메시지",
                    author1,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "세 번째 메시지",
                    author1,
                    channel1,
                    thirdTime
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Message> result =
                    messageRepository
                            .findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
                                    channel1.getId(),
                                    thirdTime,
                                    pageable
                            );

            // then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactly(
                            "두 번째 메시지",
                            "첫 번째 메시지"
                    );

            assertThat(result)
                    .extracting(Message::getCreatedAt)
                    .allMatch(createdAt ->
                            createdAt.isBefore(thirdTime)
                    );
        }

        @Test
        @DisplayName("커서보다 오래된 메시지가 없으면 빈 목록을 반환한다")
        void empty() {
            // given
            createAndSaveMessage(
                    "두 번째 메시지",
                    author1,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "세 번째 메시지",
                    author1,
                    channel1,
                    thirdTime
            );

            Pageable pageable = PageRequest.of(0, 10);

            // when
            List<Message> result =
                    messageRepository
                            .findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
                                    channel1.getId(),
                                    firstTime,
                                    pageable
                            );

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다음 페이지에서도 요청한 크기만큼 조회한다")
        void paging() {
            // given
            Instant fourthTime =
                    Instant.parse("2026-07-28T04:00:00Z");

            createAndSaveMessage(
                    "첫 번째 메시지",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "두 번째 메시지",
                    author1,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "세 번째 메시지",
                    author1,
                    channel1,
                    thirdTime
            );

            createAndSaveMessage(
                    "네 번째 메시지",
                    author1,
                    channel1,
                    fourthTime
            );

            Pageable pageable = PageRequest.of(0, 2);

            // when
            List<Message> result =
                    messageRepository
                            .findByChannel_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
                                    channel1.getId(),
                                    fourthTime,
                                    pageable
                            );

            // then
            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(Message::getContent)
                    .containsExactly(
                            "세 번째 메시지",
                            "두 번째 메시지"
                    );
        }
    }

    @Nested
    @DisplayName("최근 메시지 생성 시간 조회")
    class FindLastMessageAt {

        @Test
        @DisplayName("채널의 가장 최근 메시지 생성 시간을 반환한다")
        void success() {
            // given
            createAndSaveMessage(
                    "첫 번째 메시지",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "두 번째 메시지",
                    author1,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "세 번째 메시지",
                    author1,
                    channel1,
                    thirdTime
            );

            createAndSaveMessage(
                    "다른 채널 메시지",
                    author1,
                    channel2,
                    Instant.parse("2026-07-28T10:00:00Z")
            );

            // when
            Instant result =
                    messageRepository.findLastMessageAtByChannelId(
                            channel1.getId()
                    );

            // then
            assertThat(result).isEqualTo(thirdTime);
        }

        @Test
        @DisplayName("채널에 메시지가 없으면 null을 반환한다")
        void empty() {
            // given
            createAndSaveMessage(
                    "다른 채널 메시지",
                    author1,
                    channel2,
                    firstTime
            );

            // when
            Instant result =
                    messageRepository.findLastMessageAtByChannelId(
                            channel1.getId()
                    );

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("채널별 메시지 삭제")
    class DeleteByChannelId {

        @Test
        @DisplayName("특정 채널의 모든 메시지를 삭제한다")
        void success() {
            // given
            createAndSaveMessage(
                    "채널 1 메시지 1",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "채널 1 메시지 2",
                    author2,
                    channel1,
                    secondTime
            );

            createAndSaveMessage(
                    "채널 2 메시지",
                    author1,
                    channel2,
                    thirdTime
            );

            // when
            messageRepository.deleteByChannel_Id(channel1.getId());
            messageRepository.flush();

            // then
            List<Message> channel1Messages =
                    messageRepository.findAllByChannel_Id(
                            channel1.getId()
                    );

            List<Message> channel2Messages =
                    messageRepository.findAllByChannel_Id(
                            channel2.getId()
                    );

            assertThat(channel1Messages).isEmpty();
            assertThat(channel2Messages).hasSize(1);
            assertThat(messageRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("삭제할 채널 메시지가 없어도 예외가 발생하지 않는다")
        void empty() {
            // given
            createAndSaveMessage(
                    "채널 2 메시지",
                    author1,
                    channel2,
                    firstTime
            );

            // when
            messageRepository.deleteByChannel_Id(channel1.getId());
            messageRepository.flush();

            // then
            assertThat(messageRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("작성자별 메시지 삭제")
    class DeleteByAuthorId {

        @Test
        @DisplayName("특정 작성자의 모든 메시지를 삭제한다")
        void success() {
            // given
            createAndSaveMessage(
                    "작성자 1 메시지 1",
                    author1,
                    channel1,
                    firstTime
            );

            createAndSaveMessage(
                    "작성자 1 메시지 2",
                    author1,
                    channel2,
                    secondTime
            );

            createAndSaveMessage(
                    "작성자 2 메시지",
                    author2,
                    channel1,
                    thirdTime
            );

            // when
            messageRepository.deleteByAuthor_Id(author1.getId());
            messageRepository.flush();

            // then
            List<Message> author1Messages =
                    messageRepository.findAllByAuthor_Id(
                            author1.getId()
                    );

            List<Message> author2Messages =
                    messageRepository.findAllByAuthor_Id(
                            author2.getId()
                    );

            assertThat(author1Messages).isEmpty();
            assertThat(author2Messages).hasSize(1);
            assertThat(messageRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("삭제할 작성자 메시지가 없어도 예외가 발생하지 않는다")
        void empty() {
            // given
            createAndSaveMessage(
                    "작성자 2 메시지",
                    author2,
                    channel1,
                    firstTime
            );

            UUID unknownAuthorId = UUID.randomUUID();

            // when
            messageRepository.deleteByAuthor_Id(unknownAuthorId);
            messageRepository.flush();

            // then
            assertThat(messageRepository.count()).isEqualTo(1);
        }
    }

    private Message createMessage(
            String content,
            User author,
            Channel channel,
            Instant createdAt
    ) {
        Message message = new Message(
                content,
                author,
                channel
        );

        ReflectionTestUtils.setField(
                message,
                "createdAt",
                createdAt
        );

        return message;
    }

    private Message createAndSaveMessage(
            String content,
            User author,
            Channel channel,
            Instant createdAt
    ) {
        Message message = createMessage(
                content,
                author,
                channel,
                createdAt
        );

        Message savedMessage =
                messageRepository.save(message);

        messageRepository.flush();

        return savedMessage;
    }
}