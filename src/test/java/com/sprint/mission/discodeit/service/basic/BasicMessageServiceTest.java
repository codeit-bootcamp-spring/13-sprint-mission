package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
class BasicMessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService basicMessageService;

  @Test
  @DisplayName("첨부파일 없이 메시지를 생성할 수 있다")
  void 메시지만_작성() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest(
        "테스트 메시지",
        channelId,
        authorId
    );

    User author = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    Channel channel = new Channel(
        "public",
        "public dsc",
        ChannelType.PUBLIC
    );

    MessageDto expected = new MessageDto(
        UUID.randomUUID(),
        null,
        null,
        request.content(),
        channelId,
        null,
        List.of()
    );

    given(userRepository.findById(authorId))
        .willReturn(Optional.of(author));

    given(channelRepository.findById(channelId))
        .willReturn(Optional.of(channel));

    given(messageMapper.toDto(any(Message.class)))
        .willReturn(expected);

    // when
    MessageDto result = basicMessageService.create(request, null);

    // then
    assertThat(result).isEqualTo(expected);

    then(userRepository).should().findById(authorId);
    then(channelRepository).should().findById(channelId);
    then(messageRepository).should().save(any(Message.class));
    then(messageMapper).should().toDto(any(Message.class));

    verifyNoInteractions(
        binaryContentRepository,
        binaryContentStorage
    );
  }

  @Test
  @DisplayName("작성자가 존재하지 않으면 메시지 생성에 실패한다")
  void 작성자_누락_메시지_생성_실패() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest(
        "테스트 메시지",
        channelId,
        authorId
    );

    given(userRepository.findById(authorId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> basicMessageService.create(request, null)
    ).isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(
        channelRepository,
        messageRepository,
        binaryContentRepository,
        binaryContentStorage,
        messageMapper
    );
  }

  @Test
  @DisplayName("채널이 존재하지 않으면 메시지 생성에 실패한다")
  void 채널_누락_메시지_생성_실패() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest(
        "테스트 메시지",
        channelId,
        authorId
    );

    User author = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    given(userRepository.findById(authorId))
        .willReturn(Optional.of(author));

    given(channelRepository.findById(channelId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> basicMessageService.create(request, null)
    ).isInstanceOf(ChannelNotFoundException.class);

    verifyNoInteractions(
        messageRepository,
        binaryContentRepository,
        binaryContentStorage,
        messageMapper
    );
  }

  @Test
  @DisplayName("메시지 내용을 수정할 수 있다")
  void 메시지_수정() {
    // given
    UUID messageId = UUID.randomUUID();

    User author = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    Channel channel = new Channel(
        "일반 채널",
        "일반 채널입니다.",
        ChannelType.PUBLIC
    );

    Message message = new Message(
        "기존 메시지",
        channel,
        author,
        List.of()
    );

    MessageUpdateRequest request =
        new MessageUpdateRequest("수정된 메시지");

    MessageDto expected = new MessageDto(
        messageId,
        null,
        null,
        request.newContent(),
        null,
        null,
        List.of()
    );

    given(messageRepository.findById(messageId))
        .willReturn(Optional.of(message));

    given(messageMapper.toDto(message))
        .willReturn(expected);

    // when
    MessageDto result =
        basicMessageService.update(messageId, request);

    // then
    assertThat(message.getContent())
        .isEqualTo(request.newContent());

    assertThat(result).isEqualTo(expected);

    then(messageMapper).should()
        .toDto(message);
  }

  @Test
  @DisplayName("메시지 삭제 시 첨부파일과 메타데이터도 함께 삭제한다")
  void 메시지_삭제_첨부파일_삭제() {
    // given
    UUID messageId = UUID.randomUUID();
    UUID attachmentId = UUID.randomUUID();

    User author = new User(
        "testUser",
        "test@test.com",
        "password",
        null
    );

    Channel channel = new Channel(
        "일반 채널",
        "일반 채널입니다.",
        ChannelType.PUBLIC
    );

    BinaryContent attachment = mock(BinaryContent.class);

    given(attachment.getId())
        .willReturn(attachmentId);

    Message message = new Message(
        "첨부파일이 있는 메시지",
        channel,
        author,
        List.of(attachment)
    );

    given(messageRepository.findById(messageId))
        .willReturn(Optional.of(message));

    // when
    basicMessageService.delete(messageId);

    // then
    InOrder inOrder = inOrder(
        binaryContentStorage,
        binaryContentRepository,
        messageRepository
    );

    inOrder.verify(binaryContentStorage)
        .delete(attachmentId);

    inOrder.verify(binaryContentRepository)
        .deleteById(attachmentId);

    inOrder.verify(messageRepository)
        .delete(message);
  }
}