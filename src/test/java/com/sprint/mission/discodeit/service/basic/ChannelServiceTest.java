package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

  @InjectMocks
  private BasicChannelService channelService;

  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelMapper channelMapper;
  @Mock
  private UserMapper userMapper;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private void mockConvertToDtoDependencies() {
    given(messageRepository.findByChannelIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
        .willReturn(new SliceImpl<>(Collections.emptyList()));
    given(readStatusRepository.findByChannelId(any()))
        .willReturn(Collections.emptyList());
    given(channelMapper.toDto(any(Channel.class), any(), any()))
        .willReturn(new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "mockName", "mockDesc",
            Collections.emptyList(), null));
  }

  @Test
  @DisplayName("createPublic - 성공: PUBLIC 채널을 정상적으로 생성")
  void createPublic_Success() {
    // Given
    String name = "General";
    String description = "General chat";
    mockConvertToDtoDependencies();

    // When
    ChannelDto result = channelService.createPublic(name, description);

    // Then
    assertThat(result).isNotNull();
    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("createPrivate - 성공: 참여자가 존재하는 PRIVATE 채널을 생성")
  void createPrivate_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    User mockUser = new User("test@test.com", "tester", "pass");
    ReflectionTestUtils.setField(mockUser, "id", userId);

    given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
    mockConvertToDtoDependencies();

    // When
    ChannelDto result = channelService.createPrivate(List.of(userId));

    // Then
    assertThat(result).isNotNull();
    then(channelRepository).should().save(any(Channel.class));
    then(readStatusRepository).should().save(any(ReadStatus.class));
  }

  @Test
  @DisplayName("createPrivate - 실패: 존재하지 않는 유저를 참여시키려 하면 예외가 발생")
  void createPrivate_Fail_UserNotFound() {
    // Given
    UUID invalidUserId = UUID.randomUUID();
    given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> channelService.createPrivate(List.of(invalidUserId)))
        .isInstanceOf(UserNotFoundException.class);

    then(readStatusRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("update - 성공: PUBLIC 채널 정보를 수정")
  void update_Success() {
    // Given
    UUID channelId = UUID.randomUUID();
    Channel publicChannel = new Channel("Old", ChannelType.PUBLIC, "Old Desc");
    ReflectionTestUtils.setField(publicChannel, "id", channelId);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(publicChannel));
    mockConvertToDtoDependencies();

    // When
    channelService.update(channelId, "New Name", "New Desc");

    // Then
    assertThat(publicChannel.getName()).isEqualTo("New Name");
    assertThat(publicChannel.getDescription()).isEqualTo("New Desc");
  }

  @Test
  @DisplayName("update - 실패: PRIVATE 채널을 수정하려 하면 예외가 발생")
  void update_Fail_PrivateChannel() {
    // Given
    UUID channelId = UUID.randomUUID();
    Channel privateChannel = new Channel(null, ChannelType.PRIVATE, null);
    ReflectionTestUtils.setField(privateChannel, "id", channelId);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    // When & Then
    assertThatThrownBy(() -> channelService.update(channelId, "New Name", "New Desc"))
        .isInstanceOf(PrivateChannelUpdateException.class);
  }

  @Test
  @DisplayName("delete - 성공: 채널 및 관련 데이터를 정상적으로 삭제")
  void delete_Success() {
    // Given
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel("Test", ChannelType.PUBLIC, "Desc");
    ReflectionTestUtils.setField(channel, "id", channelId);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    // When
    mockConvertToDtoDependencies();
    channelService.delete(channelId);

    // Then
    then(messageRepository).should().deleteByChannelId(channelId);
    then(readStatusRepository).should().deleteByChannelId(channelId);
    then(channelRepository).should().delete(channel);
  }

  @Test
  @DisplayName("findAllByUserId - 성공: 유저가 참여 중인 채널 목록을 반환")
  void findAllByUserId_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    User user = new User("test", "test", "pass");
    Channel channel = new Channel("Test", ChannelType.PUBLIC, "Desc");
    ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());
    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());

    given(readStatusRepository.findByUserId(userId)).willReturn(List.of(readStatus));
    mockConvertToDtoDependencies();

    // When
    List<ChannelDto> result = channelService.findAllByUserId(userId);

    // Then
    assertThat(result).hasSize(1);
  }
}