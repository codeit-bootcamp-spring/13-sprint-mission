package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("findAllByChannel_Id 성공 - 채널에 속한 메시지를 최신순으로 페이징 조회한다")
  void findAllByChannelId_success() {
    // given
    User author = new User("writer", "pw123456789!", "writer@example.com");
    entityManager.persist(author);
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    entityManager.persist(channel);

    for (int i = 0; i < 3; i++) {
      Message message = new Message("내용" + i, author, channel);
      entityManager.persist(message);
    }
    entityManager.flush();

    // when
    Slice<Message> result = messageRepository.findAllByChannel_Id(
        channel.getId(), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

    // then
    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  @DisplayName("findAllByChannel_Id 실패 - 존재하지 않는 채널이면 빈 결과를 반환한다")
  void findAllByChannelId_fail_emptyResult() {
    // given
    java.util.UUID nonExistentChannelId = java.util.UUID.randomUUID();

    // when
    Slice<Message> result = messageRepository.findAllByChannel_Id(
        nonExistentChannelId, PageRequest.of(0, 10));

    // then
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  @DisplayName("findAllByChannel_IdAndCreatedAtBefore 성공 - 커서 이전 메시지만 조회한다")
  void findAllByChannelIdAndCreatedAtBefore_success() throws InterruptedException {
    // given
    User author = new User("writer2", "pw123456789!", "writer2@example.com");
    entityManager.persist(author);
    Channel channel = new Channel(ChannelType.PUBLIC, "공지2", "설명");
    entityManager.persist(channel);

    Message first = new Message("첫번째", author, channel);
    entityManager.persist(first);
    entityManager.flush();

    Thread.sleep(10); // createdAt 차이를 명확히 하기 위함
    Instant cursor = Instant.now();
    Thread.sleep(10);

    Message second = new Message("두번째", author, channel);
    entityManager.persist(second);
    entityManager.flush();

    // when
    Slice<Message> result = messageRepository.findAllByChannel_IdAndCreatedAtBefore(
        channel.getId(), cursor, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

    // then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getContent()).isEqualTo("첫번째");
  }

  @Test
  @DisplayName("findAllByChannel_IdAndCreatedAtBefore 실패 - 커서보다 이전 메시지가 없으면 빈 결과를 반환한다")
  void findAllByChannelIdAndCreatedAtBefore_fail_empty() {
    // given
    User author = new User("writer3", "pw123456789!", "writer3@example.com");
    entityManager.persist(author);
    Channel channel = new Channel(ChannelType.PUBLIC, "공지3", "설명");
    entityManager.persist(channel);
    entityManager.flush();

    Instant pastCursor = Instant.now().minusSeconds(3600);

    // when
    Slice<Message> result = messageRepository.findAllByChannel_IdAndCreatedAtBefore(
        channel.getId(), pastCursor, PageRequest.of(0, 10));

    // then
    assertThat(result.getContent()).isEmpty();
  }
}