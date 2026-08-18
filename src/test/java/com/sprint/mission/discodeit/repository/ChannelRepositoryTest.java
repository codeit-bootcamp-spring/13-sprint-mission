package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReadStatusRepository readStatusRepository;

  @Test
  @DisplayName("공개 채널은 참여 여부와 관계없이 조회된다")
  void 공개_채널_조회_성공() {
    // given
    User user = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    Channel publicChannel = new Channel(
        "공개 채널",
        "누구나 볼 수 있습니다.",
        ChannelType.PUBLIC
    );

    userRepository.save(user);
    channelRepository.save(publicChannel);

    // when
    List<Channel> result =
        channelRepository.findByType(ChannelType.PUBLIC);

    // then
    assertThat(result)
        .containsExactly(publicChannel);
  }

  @Test
  @DisplayName("사용자가 참여한 비공개 채널을 조회할 수 있다")
  void 참여한_비공개_채널_조회_성공() {
    // given
    User user = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    Channel privateChannel = new Channel(
        null,
        null,
        ChannelType.PRIVATE
    );

    userRepository.save(user);
    channelRepository.save(privateChannel);

    ReadStatus readStatus = new ReadStatus(
        user,
        privateChannel,
        Instant.now()
    );

    readStatusRepository.save(readStatus);

    // when
    List<ReadStatus> result =
        readStatusRepository.findByUser_IdAndChannel_Type(
            user.getId(),
            ChannelType.PRIVATE
        );

    // then
    assertThat(result)
        .extracting(ReadStatus::getChannel)
        .containsExactly(privateChannel);
  }

  @Test
  @DisplayName("참여하지 않은 비공개 채널은 조회되지 않는다")
  void 참여하지_않은_비공개_채널_조회_실패() {
    // given
    User participant = new User(
        "participant",
        "participant@test.com",
        "password",
        null
    );

    User otherUser = new User(
        "otherUser",
        "other@test.com",
        "password",
        null
    );

    Channel privateChannel = new Channel(
        null,
        null,
        ChannelType.PRIVATE
    );

    userRepository.saveAll(
        List.of(participant, otherUser)
    );

    channelRepository.save(privateChannel);

    ReadStatus readStatus = new ReadStatus(
        participant,
        privateChannel,
        Instant.now()
    );

    readStatusRepository.save(readStatus);

    // when
    List<Channel> result =
        channelRepository.findByType(ChannelType.PUBLIC);

    // then
    assertThat(result).isEmpty();
  }
}