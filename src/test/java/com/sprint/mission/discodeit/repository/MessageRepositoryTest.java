package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest // JPA 계층 관련 빈만 로딩한다
@ActiveProfiles("test") // 테스트 실행 간 test 프로파일 활성화
@EnableJpaAuditing // JPA Audit 기능 활성화
public class MessageRepositoryTest {

  @Autowired
  MessageRepository messageRepository;

  @Autowired
  TestEntityManager entityManager;

  private User persistUser(String username, String email, String password) {
    User user = new User(username, email, password, null);
    entityManager.persist(user);
    entityManager.flush();
    entityManager.clear();
    return user;
  }

  private Channel persistChannel(ChannelType type, String name, String description) {
    Channel channel = new Channel(type, name, description);
    entityManager.persist(channel);
    entityManager.flush();
    entityManager.clear();
    return channel;
  }

  private Message persistMessage(String content, Channel channel, User author) {
    Message message = new Message(content, channel, author, null);
    entityManager.persist(message);
    entityManager.flush();
    entityManager.clear();
    return message;
  }

  @Nested
  @DisplayName("직접 작성한 Query")
  class CustomQueries {

    @Test
    @DisplayName("findByChannelId - 특정 채널의 아이디가 존재하는 메시지를 조회할 수 있다")
    void 존재하는_채널_아이디_조회() {
      // given
      User user = persistUser("사용자", "user@icloud.com", "Abcd1234!");
      Channel channel = persistChannel(ChannelType.PUBLIC, "채널", "채널 설명");
      persistMessage("첫번째 메시지 내용", channel, user);
      persistMessage("두번째 메시지 내용", channel, user);

      // when
      List<Message> foundMessages = messageRepository.findByChannelId(channel.getId());

      // then
      assertAll(
          () -> assertThat(foundMessages).hasSize(2),
          () -> assertThat(foundMessages).extracting(message -> message.getContent())
              .containsExactly("첫번째 메시지 내용", "두번째 메시지 내용"),
          () -> assertThat(foundMessages).extracting(message -> message.getAuthor().getUsername())
              .containsOnly("사용자"),
          () -> assertThat(foundMessages).isNotEmpty()
              .allMatch(message -> message.getChannel().getId().equals(channel.getId()))
      );
    }

    @Test
    @DisplayName("findByChannelId - 특정 채널의 아이디가 존재하지 않는 메시지는 조회할 수 없다")
    void 존재하지_않는_채널_아이디_조회() {
      // given
      UUID channelId = UUID.randomUUID();
      // when
      List<Message> foundMessages = messageRepository.findByChannelId(channelId);
      // then
      assertThat(foundMessages).isEmpty();
    }

    @Test
    @DisplayName("findAllByChannelId - 특정 채널의 메시지 목록을 내림차순으로 페이징해 조회할 수 있다")
    void 특정_채널_메시지_목록_정상_조회() {
      // given
      User user = persistUser("사용자", "user@icloud.com", "Abcd1234!");
      Channel channel = persistChannel(ChannelType.PUBLIC, "채널", "채널 설명");
      persistMessage("첫번째 메시지 내용", channel, user);
      persistMessage("두번째 메시지 내용", channel, user);
      Pageable pageable = PageRequest.of(0, 50, Sort.by(Direction.DESC, "createdAt"));

      // when
      Slice<Message> foundMessages = messageRepository.findAllByChannelId(channel.getId(),
          pageable);

      // then
      assertAll(
          () -> assertThat(foundMessages.getContent().get(0).getCreatedAt()).isAfter(
              foundMessages.getContent().get(1).getCreatedAt()),
          () -> assertThat(foundMessages.getContent()).hasSize(2),
          () -> assertThat(foundMessages.getContent()).extracting(message -> message.getContent())
              .containsExactly("두번째 메시지 내용", "첫번째 메시지 내용"),
          () -> assertThat(foundMessages.getContent()).extracting(
                  message -> message.getAuthor().getUsername())
              .containsOnly("사용자"),
          () -> assertThat(foundMessages.getContent())
              .allMatch(message -> message.getChannel().getId().equals(channel.getId())),
          () -> assertThat(foundMessages.hasNext()).isFalse() // 다음 페이지는 없다
      );
    }


    @Test
    @DisplayName("findAllByChannelId - 존재하지 않는 채널의 아이디로 메시지 목록을 조회하면 이는 비어있다")
    void 존재하지_않는_채널_메시지_목록_조회() {
      // given
      User user = persistUser("사용자", "user@icloud.com", "Abcd1234!");
      Channel channel = persistChannel(ChannelType.PUBLIC, "채널", "채널 설명");
      persistMessage("첫번째 메시지 내용", channel, user);
      persistMessage("두번째 메시지 내용", channel, user);
      UUID noneChannelId = UUID.randomUUID();
      Pageable pageable = PageRequest.of(0, 50, Sort.by(Direction.DESC, "createdAt"));

      // when
      Slice<Message> foundMessages = messageRepository.findAllByChannelId(noneChannelId,
          pageable);

      // then
      assertAll(
          () -> assertThat(foundMessages.getContent()).isEmpty(),
          () -> assertThat(foundMessages.hasNext()).isFalse()
      );
    }

  }
}
