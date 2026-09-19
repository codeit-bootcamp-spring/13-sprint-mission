package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@DisplayName("ChannelRepository 슬라이스 테스트")
class ChannelRepositoryTest {

  @Autowired
  ChannelRepository repository;

  @Autowired
  TestEntityManager entityManager;

  @BeforeEach
  void setUp() {
    entityManager.persist(createChannel("채널명"));
    entityManager.persist(createChannel("채널명저장"));
    entityManager.flush();
    entityManager.clear();
  }

  @Nested
  @DisplayName("페이징과 정렬")
  class PagingAndSorting {

    @Test
    @DisplayName("채널명을 기준으로 오름차순 정렬한다")
    void sortByName() {
      var page = repository.findAll(PageRequest.of(0, 1, Sort.by("name")));

      assertThat(page.getContent()).extracting(Channel::getName).containsExactly("채널명");
      assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("페이지 번호가 음수이면 예외가 발생한다")
    void rejectNegativePage() {
      assertThatThrownBy(() -> PageRequest.of(-1, 10))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  private Channel createChannel(String name) {
    return Channel.publicChannelBuilder()
        .type(ChannelType.PUBLIC)
        .name(name)
        .description("test channel")
        .build();
  }
}
