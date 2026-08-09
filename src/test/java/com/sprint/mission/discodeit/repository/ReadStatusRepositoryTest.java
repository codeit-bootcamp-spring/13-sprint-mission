package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ReadStatusRepositoryTest {

  @Autowired
  private ReadStatusRepository readStatusRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("existsByUser_IdAndChannel_Id 성공 - 존재하면 true를 반환한다")
  void existsByUserIdAndChannelId_success() {
    // given
    User user = new User("reader", "pw123456789!", "reader@example.com");
    entityManager.persist(user);
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    entityManager.persist(channel);
    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
    entityManager.persist(readStatus);
    entityManager.flush();

    // when
    boolean result = readStatusRepository.existsByUser_IdAndChannel_Id(
        user.getId(), channel.getId());

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("existsByUser_IdAndChannel_Id 실패 - 존재하지 않으면 false를 반환한다")
  void existsByUserIdAndChannelId_fail_notExists() {
    // when
    boolean result = readStatusRepository.existsByUser_IdAndChannel_Id(
        UUID.randomUUID(), UUID.randomUUID());

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("findAllByUser_Id(sort, limit) 성공 - 정렬과 limit이 적용되어 조회된다")
  void findAllByUserIdWithSortAndLimit_success() {
    // given
    User user = new User("reader2", "pw123456789!", "reader2@example.com");
    entityManager.persist(user);

    for (int i = 0; i < 5; i++) {
      Channel channel = new Channel(ChannelType.PUBLIC, "채널" + i, "설명");
      entityManager.persist(channel);
      entityManager.persist(new ReadStatus(user, channel, Instant.now()));
    }
    entityManager.flush();

    // when
    List<ReadStatus> result = readStatusRepository.findAllByUser_Id(
        user.getId(), Sort.by(Sort.Direction.DESC, "createdAt"), Limit.of(3));

    // then
    assertThat(result).hasSize(3);
  }

  @Test
  @DisplayName("findAllByUser_Id(sort, limit) 실패 - 데이터가 없으면 빈 리스트를 반환한다")
  void findAllByUserIdWithSortAndLimit_fail_empty() {
    // when
    List<ReadStatus> result = readStatusRepository.findAllByUser_Id(
        UUID.randomUUID(), Sort.by(Sort.Direction.DESC, "createdAt"), Limit.of(3));

    // then
    assertThat(result).isEmpty();
  }
}