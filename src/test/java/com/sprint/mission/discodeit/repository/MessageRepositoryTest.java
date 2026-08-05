package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    private UUID channelId;

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void setUp() {
        User user = new User("박경석", "park@gmailcom", "0000", null, null);
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        em.persist(user);
        em.persist(channel);

        for (int i = 1; i <= 5; i++){
            Message message = new Message("메시지" + i, channel, user, List.of());
            em.persist(message);
        }
        em.flush();
        channelId = channel.getId();
        em.clear();
    }

    @Test
    @DisplayName("채널별 메시지 최신순 페이징 조회 - 성공")
    void findByChannelId_페이지_성공 () {
        PageRequest pageable = PageRequest.of(0, 3);

        Slice<Message> result = messageRepository.findAllByChannelIdOrderByCreatedAtDesc(channelId, pageable);

        assertThat(result.getContent()).hasSize(3);

        assertThat(result.hasNext()).isTrue();

    }

    @Test
    @DisplayName("존재하지 않는 채널의 메시지 조회 - 빈 결과")
    void findByChannelId_없음() {
        Pageable pageable = PageRequest.of(0, 3);

        Slice<Message> result =
                messageRepository.findAllByChannelIdOrderByCreatedAtDesc(UUID.randomUUID(), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("커서 기반 메시지 조회 - 커서 이후 메시지만")
    void findByCursor_성공() {
        Instant now = Instant.now();
        Pageable pageable = PageRequest.of(0, 10);

        // 현재 시각 이전에 생성된 메시지 조회 (전부 setUp에서 만들어졌으니 다 나옴)
        Slice<Message> result =
                messageRepository.findAllByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(
                        channelId, now, pageable);

        assertThat(result.getContent()).hasSize(5);
    }
}