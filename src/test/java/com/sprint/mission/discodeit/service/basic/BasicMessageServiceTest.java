package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.InvalidMessageContentException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService messageService;

    @Test
    @DisplayName("메시지를 생성한다")
    void create_success() {
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                channelId,
                userId,
                List.of()
        );
        MessageDto expected = new MessageDto(
                UUID.randomUUID(),
                null,
                null,
                "hello",
                channelId,
                null,
                List.of()
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(userId)).willReturn(Optional.of(author));
        given(messageMapper.toDto(any(Message.class))).willReturn(expected);

        MessageDto result = messageService.create(request);

        assertThat(result).isEqualTo(expected);
        then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("내용과 첨부파일이 모두 없으면 메시지 생성에 실패한다")
    void create_fail_emptyContentAndAttachments() {
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        MessageCreateRequest request = new MessageCreateRequest(
                " ",
                channelId,
                userId,
                List.of()
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(userId)).willReturn(Optional.of(author));

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(InvalidMessageContentException.class);

        then(messageRepository).should(never()).save(any(Message.class));
    }

    @Test
    @DisplayName("존재하지 않는 채널에는 메시지를 생성할 수 없다")
    void create_fail_channelNotFound() {
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                "hello",
                channelId,
                userId,
                List.of()
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(request))
                .isInstanceOf(ChannelNotFoundException.class);

        then(messageRepository).should(never()).save(any(Message.class));
    }

    @Test
    @DisplayName("첨부파일이 있는 메시지를 생성한다")
    void create_success_withAttachment() {
        UUID channelId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        BinaryContentCreateRequest attachmentRequest = new BinaryContentCreateRequest(
                "image.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
        BinaryContent attachment = new BinaryContent("image.png", "image/png", 3L);
        MessageCreateRequest request = new MessageCreateRequest(
                null,
                channelId,
                userId,
                List.of(attachmentRequest)
        );
        MessageDto expected = new MessageDto(
                UUID.randomUUID(),
                null,
                null,
                null,
                channelId,
                null,
                List.of()
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(userId)).willReturn(Optional.of(author));
        given(binaryContentService.createEntity(attachmentRequest)).willReturn(attachment);
        given(messageMapper.toDto(any(Message.class))).willReturn(expected);

        MessageDto result = messageService.create(request);

        assertThat(result).isEqualTo(expected);
        then(binaryContentService).should().createEntity(attachmentRequest);
        then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("메시지를 수정한다")
    void update_success() {
        UUID messageId = UUID.randomUUID();
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        Message message = new Message("old", author, channel, List.of());
        MessageUpdateRequest request = new MessageUpdateRequest(messageId, "updated");
        MessageDto expected = new MessageDto(
                messageId,
                null,
                null,
                "updated",
                channel.getId(),
                null,
                List.of()
        );

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(messageMapper.toDto(message)).willReturn(expected);

        MessageDto result = messageService.update(messageId, request);

        assertThat(result).isEqualTo(expected);
        assertThat(message.getContent()).isEqualTo("updated");
        then(messageRepository).should().save(message);
    }

    @Test
    @DisplayName("존재하지 않는 메시지는 수정에 실패한다")
    void update_fail_messageNotFound() {
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest(messageId, "updated");

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.update(messageId, request))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    @DisplayName("채널의 메시지 목록을 조회한다")
    void findAllByChannelId_success_withoutCursor() {
        UUID channelId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 50);
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        Message message = new Message("hello", author, channel, List.of());
        MessageDto messageDto = new MessageDto(
                message.getId(),
                Instant.now(),
                null,
                "hello",
                channelId,
                null,
                List.of()
        );
        Slice<Message> messages = new SliceImpl<>(List.of(message), pageable, false);
        PageResponse<MessageDto> expected = new PageResponse<>(List.of(messageDto), null, 50, false, null);

        given(messageRepository.findAllByChannel_Id(channelId, pageable)).willReturn(messages);
        given(messageMapper.toDto(message)).willReturn(messageDto);
        given(pageResponseMapper.<MessageDto>fromSlice(any(), any())).willReturn(expected);

        PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null, pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("커서가 있으면 커서 이전 메시지 목록을 조회한다")
    void findAllByChannelId_success_withCursor() {
        UUID channelId = UUID.randomUUID();
        Instant cursor = Instant.now();
        Pageable pageable = PageRequest.of(0, 50);
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        Message message = new Message("old message", author, channel, List.of());
        MessageDto messageDto = new MessageDto(
                message.getId(),
                cursor.minusSeconds(10),
                null,
                "old message",
                channelId,
                null,
                List.of()
        );
        Slice<Message> messages = new SliceImpl<>(List.of(message), pageable, false);
        PageResponse<MessageDto> expected = new PageResponse<>(List.of(messageDto), null, 50, false, null);

        given(messageRepository.findAllByChannel_IdAndCreatedAtLessThan(channelId, cursor, pageable)).willReturn(messages);
        given(messageMapper.toDto(message)).willReturn(messageDto);
        given(pageResponseMapper.<MessageDto>fromSlice(any(), any())).willReturn(expected);

        PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, cursor, pageable);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("메시지를 삭제한다")
    void delete_success() {
        UUID messageId = UUID.randomUUID();
        Channel channel = new Channel("general", "public", null, ChannelType.PUBLIC);
        User author = new User("tester", "tester@example.com", "password", null);
        BinaryContent attachment = new BinaryContent("image.png", "image/png", 3L);
        Message message = new Message("hello", author, channel, List.of(attachment));

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        messageService.delete(messageId);

        then(messageRepository).should().delete(message);
        then(binaryContentService).should().delete(attachment.getId());
    }

    @Test
    @DisplayName("존재하지 않는 메시지는 삭제에 실패한다")
    void delete_fail_messageNotFound() {
        UUID messageId = UUID.randomUUID();

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.delete(messageId))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageRepository).should(never()).delete(any(Message.class));
    }
}