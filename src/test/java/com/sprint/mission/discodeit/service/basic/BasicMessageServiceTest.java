package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService basicMessageService;

  @Test
  @DisplayName("메시지 생성 성공")
  void create_success() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    User author = new User("writer", "pw", "writer@example.com");
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId,
        List.of());

    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(binaryContentRepository.findAllByIdIn(List.of())).willReturn(List.of());
    given(messageRepository.save(any(Message.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
    given(messageMapper.toDto(any(Message.class)))
        .willReturn(new MessageDto(UUID.randomUUID(), Instant.now(), null, "안녕하세요", channelId, null,
            List.of()));

    // when
    MessageDto result = basicMessageService.create(request);

    // then
    assertThat(result.content()).isEqualTo("안녕하세요");
    verify(messageRepository).save(any(Message.class));
  }

  @Test
  @DisplayName("메시지 생성 실패 - 존재하지 않는 채널이면 예외가 발생한다")
  void create_fail_channelNotFound() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    User author = new User("writer", "pw", "writer@example.com");
    MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId,
        List.of());

    given(userRepository.findById(authorId)).willReturn(Optional.of(author));
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicMessageService.create(request))
        .isInstanceOf(ChannelNotFoundException.class);
  }

  @Test
  @DisplayName("메시지 수정 성공")
  void update_success() {
    // given
    UUID messageId = UUID.randomUUID();
    User author = new User("writer", "pw", "writer@example.com");
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    Message message = new Message("기존내용", author, channel);
    MessageUpdateRequest request = new MessageUpdateRequest("수정된내용");

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(messageMapper.toDto(message))
        .willReturn(new MessageDto(messageId, Instant.now(), Instant.now(), "수정된내용", null, null,
            List.of()));

    // when
    MessageDto result = basicMessageService.update(messageId, request);

    // then
    assertThat(result.content()).isEqualTo("수정된내용");
  }

  @Test
  @DisplayName("메시지 수정 실패 - 존재하지 않는 messageId면 예외가 발생한다")
  void update_fail_messageNotFound() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() ->
        basicMessageService.update(messageId, new MessageUpdateRequest("내용")))
        .isInstanceOf(MessageNotFoundException.class);
  }

  @Test
  @DisplayName("메시지 삭제 성공")
  void delete_success() {
    // given
    UUID messageId = UUID.randomUUID();
    User author = new User("writer", "pw", "writer@example.com");
    Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    Message message = new Message("내용", author, channel);

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

    // when
    basicMessageService.delete(messageId);

    // then
    verify(messageRepository).deleteById(messageId);
  }

  @Test
  @DisplayName("메시지 삭제 실패 - 존재하지 않는 messageId면 예외가 발생한다")
  void delete_fail_messageNotFound() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicMessageService.delete(messageId))
        .isInstanceOf(MessageNotFoundException.class);

    verify(messageRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("findAllByChannelId 성공 - 커서 없이 첫 페이지 조회")
  void findAllByChannelId_success() {
    // given
    UUID channelId = UUID.randomUUID();
    Slice<Message> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 50), false);

    given(messageRepository.findAllByChannel_Id(any(), any())).willReturn(emptySlice);
    given(pageResponseMapper.fromSlice(any(), any()))
        .willReturn(new PageResponse<>(List.of(), null, 0, false, 0L));
    // when
    PageResponse<MessageDto> result = basicMessageService.findAllByChannelId(channelId, null);

    // then
    assertThat(result).isNotNull();
    verify(messageRepository).findAllByChannel_Id(any(), any());
  }
}