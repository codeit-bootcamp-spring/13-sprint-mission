package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.isNull;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUnmodifiableException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
  @DisplayName("존재하지 않는 참여자가 있으면 비공개 채널 생성에 실패한다")
  void 비공개_채널_생성_실패_참여자_없음() {
    // given
    UUID missingUserId = UUID.randomUUID();

    PrivateChannelCreateRequest request =
        new PrivateChannelCreateRequest(
            List.of(missingUserId)
        );

    given(userRepository.findById(missingUserId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> basicChannelService.createPrivateChannel(request)
    ).isInstanceOf(UserNotFoundException.class);

    /*
     * 현재 서비스는 참여자를 조회하기 전에
     * 채널 저장 메서드를 먼저 호출한다.
     */
    then(channelRepository).should()
        .save(any(Channel.class));

    then(userRepository).should()
        .findById(missingUserId);

    verifyNoInteractions(
        readStatusRepository,
        messageRepository,
        channelMapper,
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
  @DisplayName("공개 채널의 이름과 설명을 수정할 수 있다")
  void 공개_채널_수정_성공() {
    // given
    UUID channelId = UUID.randomUUID();

    Channel channel = mock(Channel.class);

    ChannelUpdateRequest request =
        new ChannelUpdateRequest(
            "수정된 채널",
            "수정된 설명"
        );

    ChannelDto expected = new ChannelDto(
        channelId,
        ChannelType.PUBLIC,
        request.newName(),
        request.newDescription(),
        List.of(),
        null
    );

    given(channelRepository.findById(channelId))
        .willReturn(Optional.of(channel));

    given(channel.getType())
        .willReturn(ChannelType.PUBLIC);

    given(channel.getId())
        .willReturn(channelId);

    given(readStatusRepository.findByChannel_Id(channelId))
        .willReturn(List.of());

    given(
        messageRepository
            .findTopByChannel_IdOrderByCreatedAtDesc(channelId)
    ).willReturn(Optional.empty());

    given(
        channelMapper.toDto(
            channel,
            List.of(),
            null
        )
    ).willReturn(expected);

    // when
    ChannelDto result =
        basicChannelService.update(channelId, request);

    // then
    assertThat(result).isEqualTo(expected);

    then(channel).should()
        .update(
            request.newName(),
            request.newDescription()
        );

    then(channelMapper).should()
        .toDto(
            channel,
            List.of(),
            null
        );
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

  @Test
  @DisplayName("사용자가 볼 수 있는 채널 목록을 조회할 수 있다")
  void 사용자별_채널_조회_성공() {
    // given
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    Instant lastMessageAt = Instant.now();

    Channel channel = mock(Channel.class);
    User participant = mock(User.class);
    ReadStatus readStatus = mock(ReadStatus.class);

    ChannelDto expected = new ChannelDto(
        channelId,
        ChannelType.PRIVATE,
        null,
        null,
        List.of(),
        lastMessageAt
    );

    given(
        channelRepository.findVisibleChannelsByUserId(
            userId,
            ChannelType.PUBLIC
        )
    ).willReturn(List.of(channel));

    given(channel.getId())
        .willReturn(channelId);

    given(
        readStatusRepository.findByChannel_IdIn(
            List.of(channelId)
        )
    ).willReturn(List.of(readStatus));

    given(readStatus.getChannel())
        .willReturn(channel);

    given(readStatus.getUser())
        .willReturn(participant);

    given(
        messageRepository.findLastMessageAtByChannelIds(
            List.of(channelId)
        )
    ).willReturn(
        List.<Object[]>of(
            new Object[]{
                channelId,
                lastMessageAt
            }
        )
    );

    given(
        channelMapper.toDto(
            channel,
            List.of(participant),
            lastMessageAt
        )
    ).willReturn(expected);

    // when
    List<ChannelDto> result =
        basicChannelService.findAllByUserId(userId);

    // then
    assertThat(result)
        .containsExactly(expected);

    then(channelMapper).should()
        .toDto(
            channel,
            List.of(participant),
            lastMessageAt
        );
  }

  @Test
  @DisplayName("조회 가능한 채널이 없으면 빈 목록을 반환한다")
  void 사용자별_채널_조회_빈_결과() {
    // given
    UUID userId = UUID.randomUUID();

    given(
        channelRepository.findVisibleChannelsByUserId(
            userId,
            ChannelType.PUBLIC
        )
    ).willReturn(List.of());

    // when
    List<ChannelDto> result =
        basicChannelService.findAllByUserId(userId);

    // then
    assertThat(result).isEmpty();

    verifyNoInteractions(
        readStatusRepository,
        messageRepository,
        channelMapper
    );
  }

  @Test
  @DisplayName("채널 삭제 시 메시지와 첨부파일도 함께 삭제한다")
  void 채널_삭제_성공() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID attachmentId = UUID.randomUUID();

    Channel channel = mock(Channel.class);
    Message message = mock(Message.class);
    BinaryContent attachment = mock(BinaryContent.class);

    given(channelRepository.findById(channelId))
        .willReturn(Optional.of(channel));

    given(messageRepository.findByChannel_Id(channelId))
        .willReturn(List.of(message));

    given(message.getAttachments())
        .willReturn(List.of(attachment));

    given(attachment.getId())
        .willReturn(attachmentId);

    // when
    basicChannelService.delete(channelId);

    // then
    then(channelRepository).should()
        .findById(channelId);

    then(messageRepository).should()
        .findByChannel_Id(channelId);

    InOrder inOrder = inOrder(
        binaryContentStorage,
        binaryContentRepository,
        messageRepository,
        readStatusRepository,
        channelRepository
    );

    inOrder.verify(binaryContentStorage)
        .delete(attachmentId);

    inOrder.verify(binaryContentRepository)
        .deleteById(attachmentId);

    inOrder.verify(messageRepository)
        .delete(message);

    inOrder.verify(readStatusRepository)
        .deleteByChannel_Id(channelId);

    inOrder.verify(channelRepository)
        .delete(channel);
  }

  @Test
  @DisplayName("존재하지 않는 채널은 삭제할 수 없다")
  void 채널_삭제_실패_채널_없음() {
    // given
    UUID channelId = UUID.randomUUID();

    given(channelRepository.findById(channelId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> basicChannelService.delete(channelId)
    ).isInstanceOf(ChannelNotFoundException.class);

    then(channelRepository).should()
        .findById(channelId);

    then(channelRepository).shouldHaveNoMoreInteractions();

    verifyNoInteractions(
        readStatusRepository,
        messageRepository,
        channelMapper,
        binaryContentRepository,
        binaryContentStorage
    );
  }
}