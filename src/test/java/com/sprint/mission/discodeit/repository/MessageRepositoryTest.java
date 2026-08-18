package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

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
  @DisplayName("채널 메시지를 최신순으로 페이징 조회할 수 있다")
  void 채널별_메시지_페이징_최신순_조회_성공()
      throws InterruptedException {
    // given
    User author = saveUser();
    Channel channel = saveChannel();

    saveMessage(
        "첫 번째 메시지",
        channel,
        author
    );

    Thread.sleep(10);

    saveMessage(
        "두 번째 메시지",
        channel,
        author
    );

    Thread.sleep(10);

    saveMessage(
        "세 번째 메시지",
        channel,
        author
    );

    Pageable pageable = PageRequest.of(
        0,
        2,
        Sort.by(
            Sort.Direction.DESC,
            "createdAt"
        )
    );

    // when
    Slice<Message> result =
        messageRepository.findByChannel_Id(
            channel.getId(),
            pageable
        );

    // then
    assertThat(result.getContent())
        .hasSize(2)
        .extracting(Message::getContent)
        .containsExactly(
            "세 번째 메시지",
            "두 번째 메시지"
        );

    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("해당 채널에 메시지가 없으면 빈 결과를 반환한다")
  void 채널별_메시지_조회_빈_결과() {
    // given
    Channel channel = saveChannel();

    Pageable pageable = PageRequest.of(
        0,
        10,
        Sort.by(
            Sort.Direction.DESC,
            "createdAt"
        )
    );

    // when
    Slice<Message> result =
        messageRepository.findByChannel_Id(
            channel.getId(),
            pageable
        );

    // then
    assertThat(result.getContent()).isEmpty();
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("채널별 마지막 메시지 작성 시간을 조회할 수 있다")
  void 채널별_마지막_메시지_시간_조회_성공()
      throws InterruptedException {
    // given
    User author = saveUser();
    Channel channel = saveChannel();

    saveMessage(
        "이전 메시지",
        channel,
        author
    );

    Thread.sleep(10);

    Message latestMessage = saveMessage(
        "최신 메시지",
        channel,
        author
    );

    // when
    List<Object[]> result =
        messageRepository.findLastMessageAtByChannelIds(
            List.of(channel.getId())
        );

    // then
    assertThat(result).hasSize(1);

    Object[] row = result.get(0);

    assertThat(row[0])
        .isEqualTo(channel.getId());

    assertThat((Instant) row[1])
        .isCloseTo(latestMessage.getCreatedAt(),
            within(1, ChronoUnit.MILLIS)
        );
  }

  @Test
  @DisplayName("메시지가 없는 채널은 마지막 메시지 조회 결과에 포함되지 않는다")
  void 채널별_마지막_메시지_시간_조회_빈_결과() {
    // given
    Channel channel = saveChannel();

    // when
    List<Object[]> result =
        messageRepository.findLastMessageAtByChannelIds(
            List.of(channel.getId())
        );

    // then
    assertThat(result).isEmpty();
  }

  private User saveUser() {
    User user = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    return userRepository.save(user);
  }

  private Channel saveChannel() {
    Channel channel = new Channel(
        "테스트 채널",
        "테스트 채널입니다.",
        ChannelType.PUBLIC
    );

    return channelRepository.save(channel);
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