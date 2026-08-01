package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Test
    @DisplayName("채널 ID로 메시지 목록을 조회한다")
    void findAllByChannelIdSuccess() {
        // given
        User author = saveUser(
                "user01",
                "user01@example.com"
        );

        Channel channel = saveChannel(
                "일반 채널",
                "일반 메시지 채널"
        );

        messageRepository.save(
                createMessage("첫 번째 메시지", channel, author)
        );

        messageRepository.save(
                createMessage("두 번째 메시지", channel, author)
        );

        messageRepository.flush();

        PageRequest pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Direction.ASC,
                        "createdAt"
                )
        );

        // when
        Slice<Message> result =
                messageRepository.findAllByChannelId(
                        channel.getId(),
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(2);

        assertThat(result.getContent())
                .extracting(Message::getContent)
                .containsExactly(
                        "첫 번째 메시지",
                        "두 번째 메시지"
                );
    }

    @Test
    @DisplayName("메시지가 없는 채널을 조회하면 빈 목록을 반환한다")
    void findAllByChannelIdReturnsEmpty() {
        // given
        Channel channel = saveChannel(
                "빈 채널",
                "메시지가 없는 채널"
        );

        PageRequest pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        // when
        Slice<Message> result =
                messageRepository.findAllByChannelId(
                        channel.getId(),
                        pageable
                );

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("채널 메시지를 페이지 크기에 맞게 조회한다")
    void findAllByChannelIdWithPagination() {
        // given
        User author = saveUser(
                "user02",
                "user02@example.com"
        );

        Channel channel = saveChannel(
                "페이징 채널",
                "페이징 테스트용 채널"
        );

        messageRepository.save(
                createMessage("메시지 1", channel, author)
        );
        messageRepository.save(
                createMessage("메시지 2", channel, author)
        );
        messageRepository.save(
                createMessage("메시지 3", channel, author)
        );

        messageRepository.flush();

        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by(
                        Sort.Direction.ASC,
                        "createdAt"
                )
        );

        // when
        Slice<Message> result =
                messageRepository.findAllByChannelId(
                        channel.getId(),
                        pageable
                );

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);

        assertThat(result.getContent())
                .extracting(Message::getContent)
                .containsExactly(
                        "메시지 1",
                        "메시지 2"
                );
    }

    @Test
    @DisplayName("채널에서 가장 최근에 생성된 메시지를 조회한다")
    void findTopByChannelIdOrderByCreatedAtDescSuccess()
            throws InterruptedException {

        // given
        User author = saveUser(
                "user03",
                "user03@example.com"
        );

        Channel channel = saveChannel(
                "최근 메시지 채널",
                "최근 메시지 조회 테스트"
        );

        Message firstMessage = messageRepository.saveAndFlush(
                createMessage(
                        "이전 메시지",
                        channel,
                        author
                )
        );

        // createdAt 값이 명확히 다르게 생성되도록 잠시 대기
        Thread.sleep(10);

        Message latestMessage = messageRepository.saveAndFlush(
                createMessage(
                        "최신 메시지",
                        channel,
                        author
                )
        );

        // when
        Optional<Message> result =
                messageRepository
                        .findTopByChannelIdOrderByCreatedAtDesc(
                                channel.getId()
                        );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId())
                .isEqualTo(latestMessage.getId());
        assertThat(result.get().getId())
                .isNotEqualTo(firstMessage.getId());
        assertThat(result.get().getContent())
                .isEqualTo("최신 메시지");
    }

    @Test
    @DisplayName("메시지가 없는 채널의 최근 메시지를 조회하면 빈 Optional을 반환한다")
    void findTopByChannelIdOrderByCreatedAtDescReturnsEmpty() {
        // given
        Channel channel = saveChannel(
                "메시지 없는 채널",
                "최근 메시지가 없는 채널"
        );

        // when
        Optional<Message> result =
                messageRepository
                        .findTopByChannelIdOrderByCreatedAtDesc(
                                channel.getId()
                        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 메시지를 조회하면 빈 목록을 반환한다")
    void findAllByUnknownChannelIdReturnsEmpty() {
        // given
        UUID unknownChannelId = UUID.randomUUID();

        PageRequest pageable = PageRequest.of(
                0,
                10,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        // when
        Slice<Message> result =
                messageRepository.findAllByChannelId(
                        unknownChannelId,
                        pageable
                );

        // then
        assertThat(result.getContent()).isEmpty();
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

    private Message createMessage(
            String content,
            Channel channel,
            User author
    ) {
        return new Message(
                content,
                channel,
                author,
                List.of()
        );
    }
}