package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelService 단위 테스트 (Mockito)")
public class ChannelServiceTest {

  @Mock
  ChannelRepository channelRepository;
  @Mock
  ReadStatusRepository readStatusRepository;
  @Mock
  MessageRepository messageRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ChannelMapper channelMapper;

  @InjectMocks
  BasicChannelService service;

  @Nested
  @DisplayName("생성(create)")
  class Create {

    @Test
    @DisplayName("공개 채널을 정상적으로 생성한다")
    void 공개_채널_생성() {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("공개 채널",
          "공개 채널 생성 시도");
      given(channelRepository.save(any(Channel.class))).willAnswer(
          invocation -> invocation.getArgument(0)); // save 메서드 호출될 때 리턴되는 첫 번째 인자값
      given(channelMapper.toDto(any(Channel.class))).willReturn(
          ChannelDto.builder()
              .id(UUID.randomUUID())
              .type(ChannelType.PUBLIC)
              .name("공개 채널")
              .description("공개 채널 생성 시도")
              .lastMessageAt(Instant.now())
              .participants(List.of())
              .build());

      //when
      service.createPublicChannel(request);
      ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
      // then
      then(channelRepository).should().save(captor.capture());
      Channel saved = captor.getValue();
      assertAll(
          () -> assertThat(saved.getName()).isEqualTo("공개 채널"),
          () -> assertThat(saved.getType()).isEqualTo(ChannelType.PUBLIC),
          () -> assertThat(saved.getDescription()).isEqualTo("공개 채널 생성 시도")
      );
    }

    @Test
    @DisplayName("비공개 채널을 정상적으로 생성한다")
    void 비공개_채널_생성() {
      // given
      UserDto user1 = UserDto.builder()
          .id(UUID.randomUUID())
          .username("사용자")
          .email("user@icloud.com")
          .profile(null)
          .online(false)
          .build();
      UserDto user2 = UserDto.builder()
          .id(UUID.randomUUID())
          .username("다른 사용자")
          .email("otheruser@icloud.com")
          .profile(null)
          .online(false)
          .build();
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest();
      request.setParticipantIds(List.of(user1.getId(), user2.getId()));
      given(channelRepository.save(any(Channel.class))).willAnswer(
          invocation -> invocation.getArgument(0)); // save 메서드 호출될 때 리턴되는 첫 번째 인자값
      given(channelMapper.toDto(any(Channel.class))).willReturn(
          ChannelDto.builder()
              .id(UUID.randomUUID())
              .type(ChannelType.PRIVATE)
              .name(null)
              .description(null)
              .lastMessageAt(Instant.now())
              .participants(List.of(user1, user2))
              .build());

      //when
      service.createPrivateChannel(request);
      ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
      // then
      then(channelRepository).should().save(captor.capture());
      Channel saved = captor.getValue();
      assertAll(
          () -> assertThat(saved.getName()).isNull(),
          () -> assertThat(saved.getType()).isEqualTo(ChannelType.PRIVATE),
          () -> assertThat(saved.getDescription()).isNull()
      );
    }
  }

  @Nested
  @DisplayName("수정(update)")
  class Update {

    @Test
    @DisplayName("공개 채널 정보를 정상적으로 수정한다")
    void 공개_채널_수정() {
      // given
      UUID channelId = UUID.randomUUID();
      Channel channel = new Channel(ChannelType.PUBLIC, "공개 채널", "공개 채널 생성");
      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(channelMapper.toDto(any(Channel.class))).willAnswer(invocation -> {
        Channel c = invocation.getArgument(0);
        return ChannelDto.builder()
            .id(c.getId())
            .type(c.getType())
            .name(c.getName())
            .description(c.getDescription())
            .lastMessageAt(c.getUpdatedAt())
            .participants(List.of())
            .build();
      });

      //when
      ChannelDto result = service.update(channelId,
          new PublicChannelUpdateRequest("수정한 공개 채널", "공개 채널 수정"));
      // then
      then(channelRepository).should().findById(channelId);
      assertAll(
          () -> assertThat(result.getName()).isEqualTo("수정한 공개 채널"),
          () -> assertThat(result.getDescription()).isEqualTo("공개 채널 수정"),
          () -> assertThat(result.getType()).isEqualTo(ChannelType.PUBLIC)
      );
    }

    @Test
    @DisplayName("비공개 채널을 변경하려면 실패한다")
    void 비공개_채널_변경() {
      // given
      UUID channelId = UUID.randomUUID();
      Channel channel = new Channel(ChannelType.PRIVATE, "비공개 채널", "비공개 채널");
      ReflectionTestUtils.setField(channel, "id", channelId);
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("수정한 비공개 채널",
          "비공개 채널 수정 시도");
      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      // when & then
      assertThatThrownBy(
          () -> service.update(channelId, request)).isInstanceOf(
          PrivateChannelUpdateException.class);
      then(channelMapper).should(never()).toDto(any());
      then(channelRepository).should().findById(channelId);
    }
  }

  @Nested
  @DisplayName("삭제(delete)")
  class Delete {

    @Test
    @DisplayName("채널을 정상적으로 삭제한다")
    void 채널_삭제() {
      // given
      UUID channelId = UUID.randomUUID();
      given(channelRepository.existsById(channelId)).willReturn(true);
      given(messageRepository.findByChannelId(channelId)).willReturn(
          List.of(mock(Message.class)));
      given(readStatusRepository.findByChannelId(channelId)).willReturn(
          List.of(mock(ReadStatus.class)));
      //when
      service.delete(channelId);
      // then
      then(channelRepository).should().existsById(channelId);
      then(messageRepository).should().findByChannelId(channelId);
      then(readStatusRepository).should().findByChannelId(channelId);
      then(channelRepository).should().deleteById(channelId);
    }

    @Test
    @DisplayName("존재하지 않는 채널에 대해 삭제를 시도하면 실패한다")
    void 존재하지_않는_채널_삭제() {
      // given
      UUID channelId = UUID.randomUUID();
      given(channelRepository.existsById(channelId)).willReturn(false);
      //when
      assertThatThrownBy(() -> service.delete(channelId)).isInstanceOf(
          ChannelNotFoundException.class);
      // then
      then(channelRepository).should().existsById(channelId);
      then(channelRepository).should(never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("사용자 아이디를 이용한 조회(findByUserId)")
  class findByUserId {

    @Test
    @DisplayName("사용자 아이디를 통해 정상적으로 조회한다")
    void 사용자_아이디_조회() {
      // given
      UUID userId = UUID.randomUUID();
      Channel channel = new Channel(ChannelType.PUBLIC, "공개 채널", "공개 채널 생성");
      ReadStatus readStatus = mock(ReadStatus.class);
      given(readStatusRepository.findByUserId(userId)).willReturn(List.of(readStatus));
      given(readStatus.getChannel()).willReturn(channel);
      given(channelRepository.findAll()).willReturn(List.of(channel));
      given(channelMapper.toDto(any(Channel.class))).willAnswer(invocation -> {
        Channel c = invocation.getArgument(0);
        return ChannelDto.builder()
            .id(c.getId())
            .type(c.getType())
            .name(c.getName())
            .description(c.getDescription())
            .lastMessageAt(c.getUpdatedAt())
            .participants(List.of())
            .build();
      });

      //when & then
      List<ChannelDto> result = service.findAllByUserId(userId);
      then(readStatusRepository).should().findByUserId(userId);
      then(channelRepository).should().findAll();
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getType()).isEqualTo(ChannelType.PUBLIC);
    }
  }
}
