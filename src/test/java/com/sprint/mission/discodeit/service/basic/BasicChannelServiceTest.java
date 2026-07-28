package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.isNull;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUnmodifiableException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelMapper channelMapper;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicChannelService basicChannelService;

  @Test
  @DisplayName("공개 채널을 생성할 수 있다.")
  void 공개_채널_생성() {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest(
        "일반 채널",
        "일반 채널입니다."
    );

    ChannelDto expected = new ChannelDto(
        null,
        ChannelType.PUBLIC,
        request.name(),
        request.description(),
        List.of(),
        null
    );

    given(channelMapper.toDto(
        any(Channel.class),
        anyList(),
        isNull()
    )).willReturn(expected);

    // when
    ChannelDto result = basicChannelService.createPublicChannel(request);

    // then
    assertThat(result).isEqualTo(expected);

    then(channelRepository).should().save(any(Channel.class));
    then(channelMapper).should().toDto(any(Channel.class), anyList(), isNull());

    verifyNoInteractions(
        readStatusRepository,
        messageRepository,
        userRepository,
        binaryContentRepository,
        binaryContentStorage
    );
  }

  @Test
  @DisplayName("참여자를 포함한 비공개 채널을 생성할 수 있다.")
  void 비공개_채널_생성() {
    // given
    UUID userId = UUID.randomUUID();

    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(userId)
    );

    User participant = new User(
        "participant",
        "participant@test.com",
        "password",
        null
    );

    ChannelDto expected = new ChannelDto(
        null,
        ChannelType.PRIVATE,
        null,
        null,
        List.of(),
        null
    );

    given(userRepository.findById(userId))
        .willReturn(Optional.of(participant));

    given(channelMapper.toDto(
        any(Channel.class),
        anyList(),
        isNull()
    )).willReturn(expected);

    // when
    ChannelDto result = basicChannelService.createPrivateChannel(request);

    // then
    assertThat(result).isEqualTo(expected);

    then(channelRepository).should().save(any(Channel.class));
    then(userRepository).should().findById(userId);
    then(readStatusRepository).should().save(any(ReadStatus.class));
    then(channelMapper).should().toDto(any(Channel.class), anyList(), isNull());

    verifyNoInteractions(
        messageRepository,
        binaryContentRepository,
        binaryContentStorage
    );
  }

  @Test
  @DisplayName("존재하지 않는 채널을 조회하면 예외가 발생한다.")
  void 존재_않는_채널_조회_예외() {
    // given
    UUID channelId = UUID.randomUUID();

    given(channelRepository.findById(channelId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> basicChannelService.findById(channelId)
    ).isInstanceOf(ChannelNotFoundException.class);

    then(channelMapper).shouldHaveNoInteractions();
    then(readStatusRepository).shouldHaveNoInteractions();
    then(messageRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("비공개 채널은 수정할 수 없다.")
  void 비공개_채널_수정_불가() {
    // given
    UUID channelId = UUID.randomUUID();

    Channel privateChannel = new Channel(
        null,
        null,
        ChannelType.PRIVATE
    );

    ChannelUpdateRequest request = new ChannelUpdateRequest(
        "수정된 채널",
        "수정된 설명"
    );

    given(channelRepository.findById(channelId))
        .willReturn(Optional.of(privateChannel));

    // when & then
    assertThatThrownBy(
        () -> basicChannelService.update(channelId, request)
    ).isInstanceOf(PrivateChannelUnmodifiableException.class);

    then(channelMapper).shouldHaveNoInteractions();
    then(readStatusRepository).shouldHaveNoInteractions();
    then(messageRepository).shouldHaveNoInteractions();
  }
}