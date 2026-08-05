package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllByChannelId_페이징과_정렬_조건에_맞게_조회한다() {
        Channel channel = channelRepository.save(
                new Channel("general", "일반 채널", ChannelType.PUBLIC)
        );
        User author = userRepository.save(
                new User("codeit", "codeit@example.com", "password123")
        );

        messageRepository.save(new Message("message-3", channel, author));
        messageRepository.save(new Message("message-1", channel, author));
        messageRepository.save(new Message("message-2", channel, author));
        messageRepository.flush();

        Pageable pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.ASC, "content")
        );

        Slice<Message> result =
                messageRepository.findAllByChannelId(channel.getId(), pageable);

        assertThat(result.getContent())
                .extracting(Message::getContent)
                .containsExactly("message-1", "message-2");
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void findAllByChannelId_메시지가_없으면_빈_슬라이스를_반환한다() {
        Pageable pageable = PageRequest.of(
                0,
                50,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Slice<Message> result =
                messageRepository.findAllByChannelId(UUID.randomUUID(), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }
}