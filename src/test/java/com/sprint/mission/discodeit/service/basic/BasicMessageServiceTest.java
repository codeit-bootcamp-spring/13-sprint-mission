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
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
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
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService basicMessageService;

    @Nested
    @DisplayName("(생성) 메시지 생성")
    class CreateMessage {

        @Test
        @DisplayName("사용자와 채널이 존재하면 메시지를 생성")
        void create_success() {
            // given
            UUID authorId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            MessageCreateRequest request = new MessageCreateRequest(
                    "테스트 메시지",
                    channelId,
                    authorId
            );

            User author = mock(User.class);
            Channel channel = mock(Channel.class);
            MessageDto expectedDto = mock(MessageDto.class);

            given(userRepository.findById(authorId))
                    .willReturn(Optional.of(author));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            // 저장하러 들어온 Message 객체를 그대로 반환
            given(messageRepository.save(any(Message.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(messageMapper.toDto(any(Message.class)))
                    .willReturn(expectedDto);

            // when
            MessageDto result = basicMessageService.createMessage(request, null);

            // then
            assertThat(result).isEqualTo(expectedDto);

            ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);

            then(messageRepository).should()
                    .save(messageCaptor.capture());

            Message savedMessage = messageCaptor.getValue();

            assertThat(savedMessage.getContent())
                    .isEqualTo(request.content());

            assertThat(savedMessage.getAuthor())
                    .isEqualTo(author);

            assertThat(savedMessage.getChannel())
                    .isEqualTo(channel);

            assertThat(savedMessage.getAttachments())
                    .isEmpty();

            then(userRepository).should()
                    .findById(authorId);
            then(channelRepository).should()
                    .findById(channelId);
            then(messageMapper).should()
                    .toDto(any(Message.class));
        }

        @Test
        @DisplayName("작성자가 존재하지 않으면 예외가 발생")
        void create_fail() {
            // given
            UUID authorId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            MessageCreateRequest request = new MessageCreateRequest(
                    "안녕하세요.",
                    channelId,
                    authorId
            );

            given(userRepository.findById(authorId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicMessageService.createMessage(request, null))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .findById(authorId);

            then(messageRepository).should(never())
                    .save(any(Message.class));
        }
    }

    @Nested
    @DisplayName("(수정) 메시지 수정")
    class UpdateMessage {

        @Test
        @DisplayName("메시지가 존재하면 내용을 수정한다")
        void update_success() {
            // given
            UUID messageId = UUID.randomUUID();

            Message message = mock(Message.class);
            MessageUpdateRequest request = new MessageUpdateRequest("수정된 메시지입니다.");

            MessageDto expectedDto = mock(MessageDto.class);

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            given(messageMapper.toDto(message))
                    .willReturn(expectedDto);

            // when
            MessageDto result = basicMessageService.updateMessage(messageId, request);

            // then
            assertThat(result).isEqualTo(expectedDto);

            then(messageRepository).should()
                    .findById(messageId);

            then(message).should()
                    .updateMessage(request.newContent());

            then(messageMapper).should()
                    .toDto(message);
        }

        @Test
        @DisplayName("수정할 메시지가 존재하지 않으면 예외가 발생")
        void update_fail() {
            // given
            UUID messageId = UUID.randomUUID();

            MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicMessageService.updateMessage(messageId, request)).
                    isInstanceOf(MessageNotFoundException.class);

            then(messageRepository).should()
                    .findById(messageId);
        }
    }

    @Nested
    @DisplayName("(삭제) 메시지 삭제")
    class DeleteMessage {

        @Test
        @DisplayName("메시지가 존재하면 삭제")
        void delete_success() {
            // given
            UUID messageId = UUID.randomUUID();

            Message message = mock(Message.class);

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            given(message.getId())
                    .willReturn(messageId);

            given(message.getAttachments())
                    .willReturn(List.of());

            // when
            basicMessageService.deleteMessage(messageId);

            // then
            then(messageRepository).should()
                    .findById(messageId);
            then(messageRepository).should()
                    .deleteById(messageId);
        }

        @Test
        @DisplayName("삭제할 메시지가 존재하지 않으면 예외가 발생")
        void delete_fail() {
            // given
            UUID messageId = UUID.randomUUID();

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicMessageService.deleteMessage(messageId))
                    .isInstanceOf(MessageNotFoundException.class);

            then(messageRepository).should()
                    .findById(messageId);

            then(messageRepository).should(never())
                    .deleteById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("(조회) 채널별 메시지 조회")
    class GetMessagesByChannelId {

        @Test
        @DisplayName("커서가 없으면 첫 페이지를 조회")
        void get_success() {
            // given
            UUID channelId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            Message message = mock(Message.class);
            MessageDto messageDto = mock(MessageDto.class);
            PageResponse<MessageDto> expectedResponse = mock(PageResponse.class);

            Slice<Message> messageSlice = new SliceImpl<>(List.of(message), pageable, false);

            given(channelRepository.existsById(channelId))
                    .willReturn(true);

            given(messageRepository.findAllByChannelId(channelId, pageable))
                    .willReturn(messageSlice);

            given(messageMapper.toDto(message))
                    .willReturn(messageDto);

            given(pageResponseMapper.fromSlice(any(Slice.class)))
                    .willReturn(expectedResponse);

            // when
            PageResponse<MessageDto> result = basicMessageService.getMessagesByChannelId(channelId,null, pageable);

            // then
            assertThat(result)
                    .isEqualTo(expectedResponse);

            then(messageRepository).should()
                    .findAllByChannelId(channelId, pageable);

            then(messageRepository).should(never())
                    .findAllByChannelId(any(UUID.class), any(Instant.class), any(Pageable.class));

            then(messageMapper).should()
                    .toDto(message);
            then(pageResponseMapper).should()
                    .fromSlice(any(Slice.class));
        }

        @Test
        @DisplayName("채널이 존재하지 않으면 예외가 발생")
        void get_fail() {
            // given
            UUID channelId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            given(channelRepository.existsById(channelId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> basicMessageService.getMessagesByChannelId(channelId, null, pageable))
                    .isInstanceOf(ChannelNotFoundException.class);
        }
    }
}