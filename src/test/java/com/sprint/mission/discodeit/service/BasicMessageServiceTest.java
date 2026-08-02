package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private MessageMapper messageMapper;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService messageService;

    @Test
    @DisplayName("메시지 생성 성공 - 채널과 작성자가 존재하면 메시지를 생성한다")
    void create_성공() {
        // given
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                "안녕하세요!", channelId, authorId
        );
        Channel channel = new Channel(ChannelType.PUBLIC, "테스트채널", "설명");
        User author = new User("testuser", "test@test.com", "password", null);
        Message message = new Message("안녕하세요!", channel, author, List.of());
        MessageDto expectedDto = new MessageDto(
                message.getId(), Instant.now(), null,
                "안녕하세요!", channelId, null, List.of()
        );

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(authorId)).willReturn(Optional.of(author));
        given(messageRepository.save(any(Message.class))).willReturn(message);
        given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

        // when
        MessageDto result = messageService.create(request, List.of());

        // then
        assertThat(result.content()).isEqualTo("안녕하세요!");
        then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("메시지 생성 실패 - 존재하지 않는 채널이면 예외 발생")
    void create_실패_채널없음() {
        // given
        UUID notExistChannelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                "안녕하세요!", notExistChannelId, authorId
        );
        given(channelRepository.findById(notExistChannelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.create(request, List.of()))
                .isInstanceOf(ChannelNotFoundException.class);

        then(messageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("메시지 생성 실패 - 존재하지 않는 작성자면 예외 발생")
    void create_실패_작성자없음() {
        // given
        UUID channelId = UUID.randomUUID();
        UUID notExistAuthorId = UUID.randomUUID();
        MessageCreateRequest request = new MessageCreateRequest(
                "안녕하세요!", channelId, notExistAuthorId
        );
        Channel channel = new Channel(ChannelType.PUBLIC, "테스트채널", "설명");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepository.findById(notExistAuthorId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.create(request, List.of()))
                .isInstanceOf(UserNotFoundException.class);

        then(messageRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("메시지 수정 성공 - 존재하는 메시지면 내용을 수정한다")
    void update_성공() {
        // given
        UUID messageId = UUID.randomUUID();
        Channel channel = new Channel(ChannelType.PUBLIC, "채널", "설명");
        User author = new User("testuser", "test@test.com", "password", null);
        Message message = new Message("기존내용", channel, author, List.of());
        MessageUpdateRequest request = new MessageUpdateRequest("수정된내용");
        MessageDto expectedDto = new MessageDto(
                messageId, Instant.now(), Instant.now(),
                "수정된내용", channel.getId(), null, List.of()
        );

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

        // when
        MessageDto result = messageService.update(messageId, request);

        // then
        assertThat(result.content()).isEqualTo("수정된내용");
    }

    @Test
    @DisplayName("메시지 수정 실패 - 존재하지 않는 메시지면 MessageNotFoundException 발생")
    void update_실패_메시지없음() {
        // given
        UUID notExistMessageId = UUID.randomUUID();
        MessageUpdateRequest request = new MessageUpdateRequest("수정된내용");
        given(messageRepository.findById(notExistMessageId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> messageService.update(notExistMessageId, request))
                .isInstanceOf(MessageNotFoundException.class);
    }

    @Test
    @DisplayName("메시지 삭제 성공 - 존재하는 메시지면 deleteById가 호출된다")
    void delete_성공() {
        // given
        UUID messageId = UUID.randomUUID();
        given(messageRepository.existsById(messageId)).willReturn(true);

        // when
        messageService.delete(messageId);

        // then
        then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 존재하지 않는 메시지면 MessageNotFoundException 발생")
    void delete_실패_메시지없음() {
        // given
        UUID notExistMessageId = UUID.randomUUID();
        given(messageRepository.existsById(notExistMessageId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> messageService.delete(notExistMessageId))
                .isInstanceOf(MessageNotFoundException.class);

        then(messageRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("채널별 메시지 목록 조회 성공 - 메시지가 있으면 PageResponse로 반환한다")
    void findAllByChannelId_성공() {
        // given
        UUID channelId = UUID.randomUUID();
        Instant cursor = Instant.now();
        Pageable pageable = PageRequest.of(0, 50);

        Channel channel = new Channel(ChannelType.PUBLIC, "채널", "설명");
        User author = new User("testuser", "test@test.com", "password", null);
        Message message = new Message("테스트메시지", channel, author, List.of());
        MessageDto messageDto = new MessageDto(
                message.getId(), Instant.now(), null, "테스트메시지", channelId, null, List.of()
        );

        given(messageRepository.findAllByChannelIdWithAuthor(any(), any(), any()))
                .willReturn(new SliceImpl<>(List.of(message), pageable, false));
        given(messageMapper.toDto(any(Message.class))).willReturn(messageDto);
        given(pageResponseMapper.fromSlice(any(), any()))
                .willReturn(new PageResponse<>(
                        List.of(messageDto),
                        null,
                        50,
                        false,
                        null // totalElements
                ));

        // when
        PageResponse<MessageDto> result = messageService.findAllByChannelId(
                channelId, cursor, pageable
        );

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("채널별 메시지 목록 조회 - 메시지가 없으면 빈 리스트를 반환한다")
    void findAllByChannelId_빈목록() {
        // given
        UUID channelId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 50);

        given(messageRepository.findAllByChannelIdWithAuthor(any(), any(), any()))
                .willReturn(new SliceImpl<>(List.of(), pageable, false));
        given(pageResponseMapper.fromSlice(any(), any()))
                .willReturn(new PageResponse<>(
                        List.of(),
                        null,    // nextCursor
                        50,      // size
                        false,   // hasNext
                        null
                ));

        // when
        PageResponse<MessageDto> result = messageService.findAllByChannelId(
                channelId, Instant.now(), pageable
        );

        // then
        assertThat(result.content()).isEmpty();
    }
}