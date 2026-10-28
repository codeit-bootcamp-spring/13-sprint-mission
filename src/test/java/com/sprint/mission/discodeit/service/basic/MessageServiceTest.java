package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @InjectMocks
  private BasicMessageService messageService;

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private MessageMapper messageMapper;

  @Test
  @DisplayName("create - 성공: 메시지를 정상적으로 생성")
  void create_Success() {
    // Given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    String content = "Hello World";

    Channel mockChannel = new Channel("General", ChannelType.PUBLIC, "Desc");
    User mockUser = new User("test@test.com", "tester", "pass");

    given(channelRepository.findById(channelId)).willReturn(Optional.of(mockChannel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(mockUser));

    MessageDto mockDto = new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), content,
        channelId, null, Collections.emptyList());
    given(messageMapper.toDto(any(Message.class))).willReturn(mockDto);

    // When
    MessageDto result = messageService.create(channelId, authorId, content, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo(content);
    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("create - 실패: 존재하지 않는 채널이면 예외가 발생")
  void create_Fail_ChannelNotFound() {
    // Given
    UUID invalidChannelId = UUID.randomUUID();
    given(channelRepository.findById(invalidChannelId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.create(invalidChannelId, UUID.randomUUID(), "Hi", null))
        .isInstanceOf(ChannelNotFoundException.class);

    then(messageRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("update - 성공: 메시지 내용을 정상적으로 수정")
  void update_Success() {
    // Given
    UUID messageId = UUID.randomUUID();
    Message mockMessage = new Message(null, null, "Old Content");
    ReflectionTestUtils.setField(mockMessage, "id", messageId);

    given(messageRepository.findById(messageId)).willReturn(Optional.of(mockMessage));

    MessageDto updatedDto = new MessageDto(messageId, Instant.now(), Instant.now(), "New Content",
        UUID.randomUUID(), null, Collections.emptyList());
    given(messageMapper.toDto(any(Message.class))).willReturn(updatedDto);

    // When
    MessageDto result = messageService.update(messageId, "New Content");

    // Then
    assertThat(result.content()).isEqualTo("New Content");
    assertThat(mockMessage.getContent()).isEqualTo("New Content");
  }

  @Test
  @DisplayName("delete - 성공: 메시지를 정상적으로 삭제")
  void delete_Success() {
    // Given
    UUID messageId = UUID.randomUUID();
    Message mockMessage = new Message(null, null, "Content");
    given(messageRepository.findById(messageId)).willReturn(Optional.of(mockMessage));

    // When
    messageService.delete(messageId);

    // Then
    then(messageRepository).should().delete(mockMessage);
  }

  @Test
  @DisplayName("delete - 실패: 존재하지 않는 메시지를 삭제하려 하면 예외가 발생")
  void delete_Fail_MessageNotFound() {
    // Given
    UUID invalidMessageId = UUID.randomUUID();
    given(messageRepository.findById(invalidMessageId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> messageService.delete(invalidMessageId))
        .isInstanceOf(MessageNotFoundException.class);
  }

  @Test
  @DisplayName("findAllByChannelId - 성공: 커서가 없을 때 최신 메시지 목록을 조회")
  void findAllByChannelId_Success_NoCursor() {
    // Given
    UUID channelId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 50);

    Message message = new Message(null, null, "Test content");
    ReflectionTestUtils.setField(message, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(message, "createdAt", Instant.now());

    SliceImpl<Message> slice = new SliceImpl<>(List.of(message), pageable, false);
    given(messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable))
        .willReturn(slice);

    // When
    PageResponse<MessageDto> response = messageService.findAllByChannelId(channelId, null,
        pageable);

    // Then
    assertThat(response.content()).hasSize(1);
    assertThat(response.hasNext()).isFalse();
  }
}