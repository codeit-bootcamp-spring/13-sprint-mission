package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private MessageMapper messageMapper;
    @Mock private BinaryContentStorage binaryContentStorage;
    @Mock private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService basicMessageService;

    @Test
    @DisplayName("메시지 생성 성공")
    void create_success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(userId, channelId, "안녕하세요", null);

        User user = mock(User.class);
        Channel channel = mock(Channel.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(messageMapper.toDto(any(Message.class)))
                .willReturn(new MessageDto(UUID.randomUUID(), Instant.now(), Instant.now(), "안녕하세요", channelId, null, List.of()));

        // when
        MessageDto result = basicMessageService.create(request);

        // then
        assertThat(result.content()).isEqualTo("안녕하세요");
        then(messageRepository).should().save(any(Message.class));
        then(binaryContentStorage).should(never()).put(any(), any());
    }

    @Test
    @DisplayName("메시지 생성 실패 - 존재하지 않는 사용자")
    void create_fail_userNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(userId, channelId, "안녕하세요", null);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicMessageService.create(request))
                .isInstanceOf(UserNotFoundException.class);

        then(messageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("메시지 생성 실패 - 존재하지 않는 채널")
    void create_fail_channelNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(userId, channelId, "안녕하세요", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(mock(User.class)));
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicMessageService.create(request))
                .isInstanceOf(ChannelNotFoundException.class);

        then(messageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("메시지 수정 성공")
    void update_success() {
        // given
        User author = mock(User.class);
        Channel channel = mock(Channel.class);
        Message message = new Message(author, channel, "이전 내용");
        UUID messageId = message.getId();
        MessageUpdateRequest request = new MessageUpdateRequest(messageId, "수정된 내용");

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(messageMapper.toDto(any(Message.class)))
                .willReturn(new MessageDto(messageId, Instant.now(), Instant.now(), "수정된 내용", UUID.randomUUID(), null, List.of()));

        // when
        MessageDto result = basicMessageService.update(request);

        // then
        assertThat(message.getContent()).isEqualTo("수정된 내용");
        assertThat(result.content()).isEqualTo("수정된 내용");
        then(messageRepository).should().save(message);
    }

    @Test
    @DisplayName("메시지 수정 실패 - 존재하지 않는 메시지")
    void update_fail_messageNotFound() {
        // given
        UUID messageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest(messageId, "수정된 내용");
        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicMessageService.update(request))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    @DisplayName("메시지 삭제 성공")
    void delete_success() {
        // given
        Message message = new Message(mock(User.class), mock(Channel.class), "내용");
        UUID messageId = message.getId();
        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        basicMessageService.delete(messageId);

        // then
        then(messageRepository).should().delete(message);
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 존재하지 않는 메시지")
    void delete_fail_messageNotFound() {
        // given
        UUID messageId = UUID.randomUUID();
        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicMessageService.delete(messageId))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    @DisplayName("채널별 메시지 조회 성공")
    void findAllByChannelId_success() {
        // given
        UUID channelId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 50);
        Message message = new Message(mock(User.class), mock(Channel.class), "내용");
        Slice<Message> slice = new SliceImpl<>(List.of(message), pageable, false);

        given(channelRepository.existsById(channelId)).willReturn(true);
        given(messageRepository.findAllByChannel_IdAndCreatedAtLessThan(eq(channelId), any(Instant.class), any(Pageable.class)))
                .willReturn(slice);
        given(messageMapper.toDto(message))
                .willReturn(new MessageDto(message.getId(), message.getCreatedAt(), message.getUpdatedAt(), "내용", channelId, null, List.of()));
        MessageDto messageDto = new MessageDto(message.getId(), message.getCreatedAt(), message.getUpdatedAt(), "내용", channelId, null, List.of());
        given(pageResponseMapper.fromSlice(any(), any()))
                .willReturn(new PageResponse<>(List.of(messageDto), null, 50, false, null));

        // when
        PageResponse<MessageDto> result = basicMessageService.findAllByChannelId(channelId, null, pageable);

        // then
        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("채널별 메시지 조회 실패 - 존재하지 않는 채널")
    void findAllByChannelId_fail_channelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 50);
        given(channelRepository.existsById(channelId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> basicMessageService.findAllByChannelId(channelId, null, pageable))
                .isInstanceOf(ChannelNotFoundException.class);
    }
}
