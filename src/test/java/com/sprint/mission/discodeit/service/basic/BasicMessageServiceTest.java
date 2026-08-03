package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageUpdateCommand;
import com.sprint.mission.discodeit.dto.message.MessageDto;
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
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private BinaryContentRepository binaryContentRepository;
    @Mock private MessageMapper messageMapper;
    @Mock private BinaryContentStorage binaryContentStorage;
    @Mock private PageResponseMapper pageResponseMapper;

    @InjectMocks private BasicMessageService messageService;

    @Test
    @DisplayName("메시지 생성 성공")
    void 메시지생성_성공() {
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        MessageCreateCommand command = new MessageCreateCommand(channelId, authorId, "안녕");

        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(authorId)).willReturn(Optional.of(user));

        MessageDto expected = new MessageDto(null, null, null, "안녕", null, null, null);
        given(messageMapper.toDto(any(Message.class))).willReturn(expected);

        MessageDto result = messageService.create(command, null);

        assertThat(result.content()).isEqualTo("안녕");
        then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("존재하지 않는 채널에 메시지 생성 실패")
    void 메시지생성_채널없음_실패() {
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        MessageCreateCommand command = new MessageCreateCommand(channelId, authorId, "안녕");
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(command, null))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    @DisplayName("메시지 수정 성공")
    void 메시지수정_성공() {
        UUID messageId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        Message message = new Message("원본", channel, user, List.of());
        MessageUpdateCommand command = new MessageUpdateCommand("수정됨");
        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        MessageDto expected = new MessageDto(null, null, null, "수정됨", null, null, null);
        given(messageMapper.toDto(any(Message.class))).willReturn(expected);

        MessageDto result = messageService.updateMessage(messageId, command);

        assertThat(result.content()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("존재하지 않는 메시지 수정 실패")
    void 메시지수정_실패() {
        UUID messageId = UUID.randomUUID();
        MessageUpdateCommand command = new MessageUpdateCommand("수정됨");
        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.updateMessage(messageId, command))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    @DisplayName("메시지 삭제 성공")
    void 메시지삭제_성공() {
        UUID messageId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        Message message = new Message("내용", channel, user, List.of());
        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        messageService.delete(messageId);

        then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 삭제 실패")
    void 메시지삭제_실패() {
        UUID messageId = UUID.randomUUID();
        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.delete(messageId))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    @DisplayName("채널별 메시지 조회 성공")
    void findByChannelId_성공() {
        UUID channelId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PUBLIC, "공지", "설명");
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        Slice<Message> emptySlice = new SliceImpl<>(List.of());
        given(messageRepository.findAllByChannelIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
                .willReturn(emptySlice);

        PageResponse<MessageDto> expected = new PageResponse<>(List.of(), null, 0, false, null);
        given(pageResponseMapper.<MessageDto>toDto(any(), any())).willReturn(expected);

        PageResponse<MessageDto> result =
                messageService.findAllByChannelIdWithCursor(channelId, null, Pageable.unpaged());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 채널의 메시지 조회 실패")
    void findByChannelId_채널없음_실패() {
        UUID channelId = UUID.randomUUID();
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                messageService.findAllByChannelIdWithCursor(channelId, null, Pageable.unpaged()))
                .isInstanceOf(ChannelNotFoundException.class);
    }
}