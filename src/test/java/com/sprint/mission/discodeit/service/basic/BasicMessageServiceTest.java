package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

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
    private BasicMessageService messageService;

    @Test
    void create_성공() {
        UUID channelId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                channelId,
                senderId,
                "안녕하세요.",
                List.of(attachmentId)
        );
        Channel channel = new Channel("공지", "공지 채널", ChannelType.PUBLIC);
        User author = new User("codeit", "codeit@example.com", "password123");
        BinaryContent attachment =
                new BinaryContent("image.png", 100L, "image/png");
        MessageResponse expected = messageResponse("안녕하세요.");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(senderId)).willReturn(Optional.of(author));
        given(binaryContentRepository.findAllById(List.of(attachmentId)))
                .willReturn(List.of(attachment));
        given(messageRepository.save(any(Message.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(messageMapper.toDto(any(Message.class))).willReturn(expected);

        MessageResponse actual = messageService.create(request);

        assertThat(actual).isEqualTo(expected);
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        then(messageRepository).should().save(messageCaptor.capture());
        Message savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getContent()).isEqualTo("안녕하세요.");
        assertThat(savedMessage.getChannel()).isSameAs(channel);
        assertThat(savedMessage.getAuthor()).isSameAs(author);
        assertThat(savedMessage.getAttachments()).containsExactly(attachment);
    }

    @Test
    void create_채널이_없으면_실패() {
        UUID channelId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                channelId,
                UUID.randomUUID(),
                "안녕하세요.",
                List.of()
        );
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(ChannelNotFoundException.class)
                .hasMessage("채널을 찾을 수 없습니다.");

        then(userRepository).shouldHaveNoInteractions();
        then(messageRepository).should(never()).save(any(Message.class));
    }

    @Test
    void update_성공() {
        UUID messageId = UUID.randomUUID();
        Message message = new Message(
                "수정 전",
                new Channel("공지", "공지 채널", ChannelType.PUBLIC),
                new User("codeit", "codeit@example.com", "password123")
        );
        MessageUpdateRequest request = new MessageUpdateRequest("수정 후");
        MessageResponse expected = messageResponse("수정 후");

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(messageMapper.toDto(message)).willReturn(expected);

        MessageResponse actual = messageService.update(messageId, request);

        assertThat(actual).isEqualTo(expected);
        assertThat(message.getContent()).isEqualTo("수정 후");
        then(messageMapper).should().toDto(message);
    }

    @Test
    void update_메시지가_없으면_실패() {
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest("수정 후");
        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.update(messageId, request))
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage("메시지를 찾을 수 없습니다.");

        then(messageMapper).shouldHaveNoInteractions();
    }

    @Test
    void delete_성공() {
        UUID messageId = UUID.randomUUID();

        messageService.delete(messageId);

        then(messageRepository).should().deleteById(messageId);
    }

    @Test
    void delete_저장소_오류가_발생하면_실패() {
        UUID messageId = UUID.randomUUID();
        willThrow(new DataAccessResourceFailureException("메시지 삭제 실패"))
                .given(messageRepository)
                .deleteById(messageId);

        assertThatThrownBy(() -> messageService.delete(messageId))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessage("메시지 삭제 실패");
    }

    @Test
    void findAllByChannelId_성공() {
        UUID channelId = UUID.randomUUID();
        Message firstMessage = message("첫 번째");
        Message secondMessage = message("두 번째");
        MessageResponse firstResponse = messageResponse("첫 번째");
        MessageResponse secondResponse = messageResponse("두 번째");

        given(messageRepository.findAllByChannelId(channelId))
                .willReturn(List.of(firstMessage, secondMessage));
        given(messageMapper.toDto(firstMessage)).willReturn(firstResponse);
        given(messageMapper.toDto(secondMessage)).willReturn(secondResponse);

        List<MessageResponse> actual = messageService.findAllByChannelId(channelId);

        assertThat(actual).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void findAllByChannelId_메시지가_없으면_빈_목록을_반환한다() {
        UUID channelId = UUID.randomUUID();
        given(messageRepository.findAllByChannelId(channelId)).willReturn(List.of());

        List<MessageResponse> actual = messageService.findAllByChannelId(channelId);

        assertThat(actual).isEmpty();
        then(messageMapper).shouldHaveNoInteractions();
    }

    private Message message(String content) {
        return new Message(
                content,
                new Channel("공지", "공지 채널", ChannelType.PUBLIC),
                new User("codeit", "codeit@example.com", "password123")
        );
    }

    private MessageResponse messageResponse(String content) {
        return new MessageResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                content,
                List.of(),
                null,
                null,
                null
        );
    }
}