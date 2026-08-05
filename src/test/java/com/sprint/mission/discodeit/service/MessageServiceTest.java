package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 슬라이스 테스트 (Mockito)")
public class MessageServiceTest {

  @Mock
  MessageRepository messageRepository;
  @Mock
  ChannelRepository channelRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  BinaryContentRepository contentRepository;
  @Mock
  MessageMapper messageMapper;
  @Mock
  BinaryContentStorage storage;

  @Spy
  PageResponseMapper pageResponseMapper;

  @InjectMocks
  BasicMessageService service;

  private Channel sampleChannel() {
    return new Channel(ChannelType.PUBLIC, "공개 채널", "공개 채널 생성 시도");
  }

  private User sampleUser() {
    return new User("사용자", "user@icloud.com", "Abcd1234!", null);
  }

  @Nested
  @DisplayName("생성(create)")
  class Create {

    @Test
    @DisplayName("메시지를 정상적으로 생성한다")
    void 메시지_정상_생성() {
      // given
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UserDto user = UserDto.builder()
          .id(authorId)
          .username("사용자")
          .email("user@icloud.com")
          .profile(null)
          .online(false)
          .build();
      Channel channel = sampleChannel();
      ReflectionTestUtils.setField(channel, "id", channelId);
      User author = sampleUser();
      ReflectionTestUtils.setField(author, "id", authorId);
      MessageCreateRequest request = new MessageCreateRequest("메시지 전송", channelId, authorId);

      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(messageRepository.save(any(Message.class))).willAnswer(
          invocation -> invocation.getArgument(0)); // save 메서드 호출될 때 리턴되는 첫 번째 인자값
      given(messageMapper.toDto(any(Message.class))).willReturn(
          MessageDto.builder()
              .id(UUID.randomUUID())
              .createdAt(Instant.now())
              .updatedAt(Instant.now())
              .channelId(channelId)
              .author(user)
              .content("메시지 전송")
              .attachments(List.of())
              .build());

      //when
      service.create(request, List.of());
      ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
      // then
      then(messageRepository).should().save(captor.capture());
      Message saved = captor.getValue();
      assertAll(
          () -> assertThat(saved.getAuthor().getId()).isEqualTo(authorId),
          () -> assertThat(saved.getAuthor().getUsername()).isEqualTo("사용자"),
          () -> assertThat(saved.getContent()).isEqualTo("메시지 전송"),
          () -> assertThat(saved.getChannel().getId()).isEqualTo(channelId)
      );
    }

    @Test
    @DisplayName("존재하지 않는 채널 아이디로 메시지 생성을 시도하면 실패한다")
    void 존재하지_않는_메시지() {
      // given
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();

      MessageCreateRequest request = new MessageCreateRequest("메시지 생성", channelId, authorId);

      given(channelRepository.findById(channelId)).willReturn(Optional.empty());
      // when & then
      assertThatThrownBy(() -> service.create(request, List.of())).isInstanceOf(
          ChannelNotFoundException.class);

      then(channelRepository).should().findById(channelId);
      then(userRepository).shouldHaveNoInteractions(); // 채널 조회 이후의 작업은 실행되지 않도록 한다
      then(messageMapper).shouldHaveNoInteractions();
      then(messageRepository).shouldHaveNoInteractions();
    }
  }

  @Nested
  @DisplayName("수정(update)")
  class Update {

    @Test
    @DisplayName("메시지 정보를 정상적으로 수정한다")
    void 메시지_정상_수정() {
      // given
      UUID messageId = UUID.randomUUID();
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UserDto user = UserDto.builder()
          .id(authorId)
          .username("사용자")
          .email("user@icloud.com")
          .profile(null)
          .online(false)
          .build();
      Channel channel = sampleChannel();
      ReflectionTestUtils.setField(channel, "id", channelId);
      User author = sampleUser();
      ReflectionTestUtils.setField(author, "id", authorId);

      Message message = new Message("메시지 생성", channel, author, List.of());
      ReflectionTestUtils.setField(message, "id", messageId);
      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
      given(messageMapper.toDto(any(Message.class))).willAnswer(invocation -> {
        Message m = invocation.getArgument(0);
        return MessageDto.builder()
            .id(m.getId())
            .createdAt(m.getCreatedAt())
            .updatedAt(m.getUpdatedAt())
            .channelId(m.getChannel().getId())
            .author(user)
            .content(m.getContent())
            .attachments(List.of())
            .build();
      });

      //when
      MessageDto result = service.update(messageId,
          new MessageUpdateRequest("메시지 수정"));
      // then
      then(messageRepository).should().findById(messageId);
      then(messageMapper).should().toDto(message);
      assertAll(
          () -> assertThat(message.getContent()).isEqualTo("메시지 수정"),
          () -> assertThat(result.getContent()).isEqualTo("메시지 수정")
      );
    }

    @Test
    @DisplayName("메시지 아이디가 없는 메시지에 대한 삭제를 시도하면 실패한다")
    void 존재하지_않는_메시지_수정() {
      // given
      UUID messageId = UUID.randomUUID();

      MessageUpdateRequest request = new MessageUpdateRequest("메시지 수정");
      given(messageRepository.findById(messageId)).willReturn(Optional.empty());
      // when & then
      assertThatThrownBy(
          () -> service.update(messageId, request)).isInstanceOf(
          MessageNotFoundException.class);
      then(messageRepository).should().findById(messageId);
      then(messageMapper).shouldHaveNoInteractions();
    }
  }


  @Nested
  @DisplayName("삭제(delete)")
  class Delete {

    @Test
    @DisplayName("메시지를 정상적으로 삭제한다")
    void 메시지_정상_삭제() {
      // given
      UUID messageId = UUID.randomUUID();
      given(messageRepository.existsById(messageId)).willReturn(true);

      //when
      service.delete(messageId);
      // then
      then(messageRepository).should().existsById(messageId);
      then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("존재하지 않는 메시지에 대해 삭제를 시도하면 실패한다")
    void 존재하지_않는_메시지_삭제() {
      // given
      UUID messageId = UUID.randomUUID();
      given(messageRepository.existsById(messageId)).willReturn(false);
      //when
      assertThatThrownBy(() -> service.delete(messageId)).isInstanceOf(
          MessageNotFoundException.class);
      // then
      then(messageRepository).should().existsById(messageId);
      then(messageRepository).should(never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("채널 아이디를 이용한 조회(findByChannelId)")
  class findByChannelId {

    @Test
    @DisplayName("채널 아이디를 통해 정상적으로 조회한다")
    void 채널_아이디_조회() {
      // given
      UUID channelId = UUID.randomUUID();
      UUID messageId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();

      Channel channel = sampleChannel();
      ReflectionTestUtils.setField(channel, "id", channelId);
      User author = sampleUser();
      ReflectionTestUtils.setField(author, "id", authorId);

      Message message = new Message("메시지 생성", channel, author, List.of());
      ReflectionTestUtils.setField(message, "id", messageId);
      UserDto user = UserDto.builder()
          .id(authorId)
          .username(author.getUsername())
          .email(author.getEmail())
          .profile(null)
          .online(false)
          .build();
      Pageable pageable = PageRequest.of(0, 50);

      Slice<Message> slice = new SliceImpl<>(List.of(message), pageable, false);

      given(messageRepository.findAllByChannelId(channelId, pageable)).willReturn(slice);
      given(messageMapper.toDto(any(Message.class))).willAnswer(invocation -> {
        Message m = invocation.getArgument(0);
        return MessageDto.builder()
            .id(m.getId())
            .createdAt(m.getCreatedAt())
            .updatedAt(m.getUpdatedAt())
            .channelId(m.getChannel().getId())
            .author(user)
            .content(m.getContent())
            .attachments(List.of())
            .build();
      });

      //when & then
      PageResponse<MessageDto> result = service.findAllByChannelId(channelId, pageable);

      then(messageRepository).should().findAllByChannelId(channelId, pageable);
      then(messageMapper).should().toDto(any(Message.class));
      assertAll(
          () -> assertThat(result.getContent()).hasSize(1),
          () -> assertThat(result.getContent().get(0).getChannelId()).isEqualTo(channelId),
          () -> assertThat(result.isHasNext()).isFalse()
      );
    }
  }
}
