package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("특정 채널의 메세지를 최근순으로 2개씩 조회한다.")
    void findByChannelId_pagingAndSorting_success() {
        // given
        User author = saveUser();

        Channel targetChannel = saveChannel("공지 채널", "공지 채널입니다.");

        Channel otherChannel = saveChannel("일반 채널", "소통 채널입니다.");

        saveMessage("첫 번째 메세지", targetChannel, author);

        saveMessage("두 번째 메세지", targetChannel, author);

        saveMessage("세 번째 메세지", targetChannel, author);

        saveMessage("일반 채널 메세지", otherChannel, author);

        PageRequest pageable = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Slice<Message> result = messageRepository.findByChannelId(targetChannel.getId(), pageable);

        // then
        assertThat(result.getContent())
                .extracting(Message::getContent)
                .containsExactly("세 번째 메세지", "두 번째 메세지");

        assertThat(result.getNumberOfElements())
                .isEqualTo(2);

        assertThat(result.hasNext())
                .isTrue();

        assertThat(result.getContent())
                .allMatch(message ->
                        message.getChannel()
                                .getId()
                                .equals(targetChannel.getId())
                );
    }

    @Test
    @DisplayName("해당 채널에 메세지가 없으면 Slice를 반환한다.")
    void findByChannelId_empty() {
        // given
        Channel channel = saveChannel("빈 채널", "빈 채널입니다.");

        PageRequest pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Slice<Message> result = messageRepository.findByChannelId(channel.getId(), pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("커서보다 오래된 메세지를 조회한다.")
    void findByCursor_pagingAndSorting_success() {
        // given
        User author = saveUser();

        Channel channel = saveChannel("테스트 채널", "테스트 채널입니다.");

        Message oldMessage = saveMessage("오늘은 8월 2일", channel, author);

        Instant cursor = Instant.now();

        saveMessage("오늘은 8월 3일", channel, author);

        PageRequest pageable = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Slice<Message> result = messageRepository.findByChannelIdAndCreatedAtLessThan(
                channel.getId(),
                cursor,
                pageable
        );

        // then
        assertThat(result.getContent())
                .containsExactly(oldMessage);
        assertThat(result.getContent())
                .extracting(Message::getContent)
                .containsExactly("오늘은 8월 2일");

        assertThat(result.getContent())
                .allMatch(message ->
                        message.getCreatedAt()
                                .isBefore(cursor));
    }

    @Test
    @DisplayName("커서보다 오래된 메시지가 없으면 빈 Slice를 반환한다")
    void findByChannelIdAndCreatedAtLessThan_empty() {
        // given
        User author = saveUser();

        Channel channel =
                saveChannel("빈 커서 테스트 채널", "테스트 채널입니다.");

        saveMessage(
                "안녕하세요.",
                channel,
                author
        );

        Instant oldCursor =
                Instant.now().minusSeconds(3600);

        PageRequest pageable = PageRequest.of(
                0,
                50,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        // when
        Slice<Message> result =
                messageRepository
                        .findByChannelIdAndCreatedAtLessThan(
                                channel.getId(),
                                oldCursor,
                                pageable
                        );

        // then
        assertThat(result.getContent())
                .isEmpty();

        assertThat(result.hasNext())
                .isFalse();
    }

    private User saveUser() {
        User user = new User(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        return userRepository.saveAndFlush(user);
    }

    private Channel saveChannel(String name, String description) {
        Channel channel = new Channel(
                name,
                description,
                ChannelType.PUBLIC
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
