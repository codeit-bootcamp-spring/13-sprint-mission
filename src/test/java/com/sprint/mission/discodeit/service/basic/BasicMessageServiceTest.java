package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.CreateMessageCommand;
import com.sprint.mission.discodeit.dto.command.UpdateMessageCommand;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class BasicMessageServiceTest {

    @InjectMocks
    private BasicMessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private BasicBinaryContentService binaryContentService;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Test
    @DisplayName("메시지 생성 성공")
    void create_success() {
        // given
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Channel channel = new Channel(
                "공지사항",
                "공지 채널",
                ChannelType.PUBLIC
        );

        User author = new User(
                "홍길동",
                "hong@test.com",
                "12345"
        );

        CreateMessageCommand command = new CreateMessageCommand(
                authorId, channelId, "안녕하세요👋"
        );

        MessageDto expected = mock(MessageDto.class);

        given(channelRepository.findById(channelId))
                .willReturn(Optional.of(channel));

        given(userRepository.findById(authorId))
                .willReturn(Optional.of(author));

        given(messageRepository.save(any(Message.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(messageMapper.toDto(any(Message.class)))
                .willReturn(expected);

        // when
        MessageDto result =
                messageService.create(command, List.of());


        // then
        assertThat(result).isSameAs(expected);

        then(channelRepository)
                .should()
                .findById(channelId);

        then(userRepository)
                .should()
                .findById(authorId);

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        then(messageRepository)
                .should()
                .save(captor.capture());

        Message savedMessage = captor.getValue();

        assertThat(savedMessage.getContent())
                .isEqualTo("안녕하세요👋");

        assertThat(savedMessage.getChannel())
                .isSameAs(channel);

        assertThat(savedMessage.getAuthor())
                .isSameAs(author);

        assertThat(savedMessage.getAttachments())
                .isEmpty();

        then(messageMapper)
                .should()
                .toDto(savedMessage);
    }

    @Test
    @DisplayName("존재하지 않는 채널에 메시지를 생성하면 예외가 발생")
    void create_fail_channelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CreateMessageCommand command =
                new CreateMessageCommand(
                        authorId,
                        channelId,
                        "안녕하세요✋"
                );

        given(channelRepository.findById(channelId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                messageService.create(command, List.of())
        ).isInstanceOf(ChannelNotFoundException.class);

        then(userRepository)
                .shouldHaveNoInteractions();

        then(messageRepository)
                .should(never())
                .save(any(Message.class));

        then(messageMapper)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("메시지 내용 수정 성공")
    void update_success() {
        // given
        UUID messageId = UUID.randomUUID();

        Channel channel = new Channel(
                "공지사항",
                "공지 채널",
                ChannelType.PUBLIC
        );

        User author = new User(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        Message message = new Message(
                "안녕하세요👋",
                channel,
                author,
                List.of()
        );

        UpdateMessageCommand command =
                new UpdateMessageCommand("반갑습니다");

        MessageDto expected = mock(MessageDto.class);

        given(messageRepository.findById(messageId))
                .willReturn(Optional.of(message));

        given(messageMapper.toDto(message))
                .willReturn(expected);

        // when
        MessageDto result =
                messageService.update(messageId, command);

        // then
        assertThat(result).isSameAs(expected);

        assertThat(message.getContent())
                .isEqualTo("반갑습니다");

        then(messageRepository)
                .should()
                .findById(messageId);

        then(messageMapper)
                .should()
                .toDto(message);
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 수정하면 예외가 발생")
    void update_fail_messageNotFound() {
        // given
        UUID messageId = UUID.randomUUID();

        UpdateMessageCommand command =
                new UpdateMessageCommand("반갑습니다");

        given(messageRepository.findById(messageId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                messageService.update(messageId, command)
        ).isInstanceOf(MessageNotFoundException.class);

        then(messageMapper)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("메시지 삭제 성공")
    void delete_success() {
        // given
        UUID messageId = UUID.randomUUID();

        Channel channel = new Channel(
                "공지사항",
                "공지 채널",
                ChannelType.PUBLIC
        );

        User author = new User(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        Message message = new Message(
                "ㅎㅇㅎㅇ",
                channel,
                author,
                List.of()
        );

        given(messageRepository.findById(messageId))
                .willReturn(Optional.of(message));

        // when
        messageService.delete(messageId);

        // then
        then(messageRepository)
                .should()
                .findById(messageId);

        then(messageRepository)
                .should()
                .delete(message);
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 삭제하면 예외가 발생")
    void delete_fail_messageNotFound() {
        // given
        UUID messageId = UUID.randomUUID();

        given(messageRepository.findById(messageId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                messageService.delete(messageId)
        ).isInstanceOf(MessageNotFoundException.class);

        then(messageRepository)
                .should(never())
                .delete(any(Message.class));
    }

    @Test
    @DisplayName("채널의 메시지 목록 조회 성공")
    void getMessages_success() {
        // given
        UUID channelId = UUID.randomUUID();

        Channel channel = new Channel(
                "공지사항",
                "공지 채널",
                ChannelType.PUBLIC
        );

        User author = new User(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        Message firstMessage = new Message(
                "안녕하세요!",
                channel,
                author,
                List.of()
        );

        Message secondMessage = new Message(
                "잘부탁드려요!",
                channel,
                author,
                List.of()
        );

        MessageDto firstDto = mock(MessageDto.class);
        MessageDto secondDto = mock(MessageDto.class);

        Pageable pageable = PageRequest.of(0, 50);

        Slice<Message> messageSlice = new SliceImpl<>(
                List.of(firstMessage, secondMessage),
                pageable,
                false
        );

        given(messageRepository.findByChannelId(
                channelId,
                pageable
        )).willReturn(messageSlice);

        given(messageMapper.toDto(firstMessage))
                .willReturn(firstDto);

        given(messageMapper.toDto(secondMessage))
                .willReturn(secondDto);

        // when
        PageResponse<MessageDto> result =
                messageService.getMessages(
                        channelId,
                        null,
                        pageable
                );

        // then
        assertThat(result.content())
                .containsExactly(firstDto, secondDto);

        assertThat(result.size())
                .isEqualTo(50);

        assertThat(result.totalElements())
                .isNull();

        assertThat(result.hasNext())
                .isFalse();

        assertThat(result.nextCursor())
                .isNull();

        then(messageRepository)
                .should()
                .findByChannelId(channelId, pageable);
    }

    @Test
    @DisplayName("채널 ID 없이 메시지 목록을 조회하면 예외가 발생")
    void getMessages_fail_channelIdIsNull() {
        // given
        Pageable pageable = PageRequest.of(0, 50,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                ));

        // when & then
        assertThatThrownBy(() ->
                messageService.getMessages(
                        null,
                        null,
                        pageable
                )
        ).isInstanceOf(IllegalArgumentException.class);

        then(messageRepository)
                .shouldHaveNoInteractions();

        then(messageMapper)
                .shouldHaveNoInteractions();
    }
}
