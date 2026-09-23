package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Role;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@DisplayName("MessageRepository 슬라이스 테스트")
class MessageRepositoryTest {

  @Autowired
  MessageRepository repository;

  @Autowired
  TestEntityManager entityManager;

  private Channel channel;

  @BeforeEach
  void setUp() {
    User author = User.builder()
        .username("neo")
        .email("neo@test.com")
        .password("password")
        .role(Role.USER)
        .build();
    channel = Channel.publicChannelBuilder()
        .type(ChannelType.PUBLIC)
        .name("general")
        .build();
    entityManager.persist(author);
    entityManager.persist(channel);
    entityManager.persist(
        Message.builder().content("first").author(author).channel(channel).build());
    entityManager.persist(
        Message.builder().content("second").author(author).channel(channel).build());
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("채널별 메시지 쿼리")
  class FindByChannelId {

    @Test
    @DisplayName("채널에 등록된 메시지를 모두 조회한다")
    void findChannelMessages() {
      assertThat(repository.findAllByChannelId(channel.getId()))
          .extracting(Message::getContent)
          .containsExactlyInAnyOrder("first", "second");
      assertThat(repository.countByChannelId(channel.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("채널이 없는 메시지를 저장하면 예외가 발생")
    void rejectMissingChannel() {
      Message message = Message.builder()
          .content("invalid message")
          .channel(null)
          .build();

      assertThatThrownBy(() -> repository.saveAndFlush(message))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

}
