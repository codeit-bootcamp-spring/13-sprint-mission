package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Test
  @DisplayName("findById 성공 - 존재하는 channelId면 Channel을 반환한다")
  void findById_success() {
    // given
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    entityManager.persist(channel);
    entityManager.flush();

    // when
    Optional<Channel> result = channelRepository.findById(channel.getId());

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("공지");
  }

  @Test
  @DisplayName("findById 실패 - 존재하지 않는 channelId면 빈 Optional을 반환한다")
  void findById_fail_notFound() {
    // when
    Optional<Channel> result = channelRepository.findById(UUID.randomUUID());

    // then
    assertThat(result).isEmpty();
  }
}