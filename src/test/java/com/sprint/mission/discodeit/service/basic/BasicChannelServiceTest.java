package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
  private ChannelMapper channelMapper;
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BasicChannelService basicChannelService;

  @Test
  @DisplayName("PUBLIC 채널 생성 성공")
  void createPublic_success() {
    // given
    String name = "공지";
    String description = "공지 채널";

    given(channelRepository.save(any(Channel.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(channelMapper.toDto(any(Channel.class)))
        .willReturn(
            new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, name, description, List.of(),
                null));

    // when
    ChannelDto result = basicChannelService.createPublic(name, description);

    // then
    assertThat(result.name()).isEqualTo(name);
    assertThat(result.type()).isEqualTo(ChannelType.PUBLIC);
    verify(channelRepository).save(any(Channel.class));
  }

  @Test
  @DisplayName("PRIVATE 채널 생성 실패 - 참여자 중 존재하지 않는 유저가 있으면 예외가 발생한다")
  void createPrivate_fail_userNotFound() {
    // given
    UUID missingUserId = UUID.randomUUID();
    given(userRepository.findById(missingUserId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicChannelService.createPrivate(List.of(missingUserId)))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("채널 수정 성공 - PUBLIC 채널이면 정상 수정된다")
  void update_success() {
    // given
    UUID channelId = UUID.randomUUID();
    Channel channel = new Channel(ChannelType.PUBLIC, "old", "oldDesc");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(channelMapper.toDto(channel))
        .willReturn(
            new ChannelDto(channelId, ChannelType.PUBLIC, "newName", "newDesc", List.of(), null));

    // when
    ChannelDto result = basicChannelService.update(channelId, "newName", "newDesc");

    // then
    assertThat(result.name()).isEqualTo("newName");
  }

  @Test
  @DisplayName("채널 수정 실패 - PRIVATE 채널이면 예외가 발생한다")
  void update_fail_privateChannel() {
    // given
    UUID channelId = UUID.randomUUID();
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

    // when & then
    assertThatThrownBy(() -> basicChannelService.update(channelId, "newName", "newDesc"))
        .isInstanceOf(PrivateChannelUpdateException.class);
  }

  @Test
  @DisplayName("채널 삭제 성공")
  void delete_success() {
    // given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(true);

    // when
    basicChannelService.delete(channelId);

    // then
    verify(channelRepository).deleteById(channelId);
  }

  @Test
  @DisplayName("채널 삭제 실패 - 존재하지 않는 channelId면 예외가 발생한다")
  void delete_fail_channelNotFound() {
    // given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> basicChannelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);

    verify(channelRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("findByUserId 성공 - 유저가 볼 수 있는 채널 목록을 반환한다")
  void findAllByUserId_success() {
    // given
    UUID userId = UUID.randomUUID();
    given(readStatusRepository.findAllByUser_Id(userId)).willReturn(List.of());
    given(channelRepository.findAll()).willReturn(List.of(
        new Channel(ChannelType.PUBLIC, "공개채널", "설명")
    ));
    given(channelMapper.toDto(any(Channel.class)))
        .willReturn(
            new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "공개채널", "설명", List.of(), null));

    // when
    List<ChannelDto> result = basicChannelService.findAllByUserId(userId);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).type()).isEqualTo(ChannelType.PUBLIC);
  }
}