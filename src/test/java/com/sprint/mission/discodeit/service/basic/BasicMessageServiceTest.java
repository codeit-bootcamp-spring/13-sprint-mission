package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private MessageMapper messageMapper;

    @Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService messageService;

    @Nested
    @DisplayName("메시지 생성")
    class CreateTest {

        @Test
        @DisplayName("메시지 생성에 성공한다")
        void createSuccess() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            CreateMessageRequest request =
                    org.mockito.Mockito.mock(CreateMessageRequest.class);

            User author =
                    org.mockito.Mockito.mock(User.class);

            Channel channel =
                    org.mockito.Mockito.mock(Channel.class);

            Message savedMessage =
                    org.mockito.Mockito.mock(Message.class);

            MessageDto expectedResponse =
                    org.mockito.Mockito.mock(MessageDto.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);
            given(request.getContent()).willReturn("테스트 메시지");

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(author));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.of(channel));

            given(messageRepository.save(any(Message.class)))
                    .willReturn(savedMessage);

            given(messageMapper.toDto(savedMessage))
                    .willReturn(expectedResponse);

            // when
            MessageDto result = messageService.create(request);

            // then
            assertThat(result).isSameAs(expectedResponse);

            ArgumentCaptor<Message> messageCaptor =
                    ArgumentCaptor.forClass(Message.class);

            then(messageRepository)
                    .should()
                    .save(messageCaptor.capture());

            assertThat(messageCaptor.getValue().getContent())
                    .isEqualTo("테스트 메시지");

            then(messageMapper)
                    .should()
                    .toDto(savedMessage);
        }

        @Test
        @DisplayName("사용자가 없으면 메시지 생성에 실패한다")
        void createFailWhenUserNotFound() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            CreateMessageRequest request =
                    org.mockito.Mockito.mock(CreateMessageRequest.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.create(request))
                    .isInstanceOf(UserNotFoundException.class);

            then(channelRepository)
                    .shouldHaveNoInteractions();

            then(messageRepository)
                    .should(never())
                    .save(any(Message.class));
        }

        @Test
        @DisplayName("채널이 없으면 메시지 생성에 실패한다")
        void createFailWhenChannelNotFound() {
            // given
            UUID userId = UUID.randomUUID();
            UUID channelId = UUID.randomUUID();

            CreateMessageRequest request =
                    org.mockito.Mockito.mock(CreateMessageRequest.class);

            User author =
                    org.mockito.Mockito.mock(User.class);

            given(request.getUserId()).willReturn(userId);
            given(request.getChannelId()).willReturn(channelId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(author));

            given(channelRepository.findById(channelId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.create(request))
                    .isInstanceOf(ChannelNotFoundException.class);

            then(messageRepository)
                    .should(never())
                    .save(any(Message.class));
        }
    }

    @Nested
    @DisplayName("메시지 조회")
    class FindTest {

        @Test
        @DisplayName("메시지 조회에 성공한다")
        void findSuccess() {
            // given
            UUID messageId = UUID.randomUUID();

            Message message =
                    org.mockito.Mockito.mock(Message.class);

            MessageDto expectedResponse =
                    org.mockito.Mockito.mock(MessageDto.class);

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            given(messageMapper.toDto(message))
                    .willReturn(expectedResponse);

            // when
            MessageDto result = messageService.find(messageId);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(messageRepository)
                    .should()
                    .findById(messageId);

            then(messageMapper)
                    .should()
                    .toDto(message);
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 조회하면 실패한다")
        void findFailWhenMessageNotFound() {
            // given
            UUID messageId = UUID.randomUUID();

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.find(messageId))
                    .isInstanceOf(MessageNotFoundException.class);

            then(messageMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("채널별 메시지 조회")
    class FindAllByChannelIdTest {

        @Test
        @DisplayName("채널별 메시지 목록 조회에 성공한다")
        void findAllByChannelIdSuccess() {
            // given
            UUID channelId = UUID.randomUUID();

            Message message =
                    org.mockito.Mockito.mock(Message.class);

            MessageDto messageDto =
                    org.mockito.Mockito.mock(MessageDto.class);

            PageResponse<MessageDto> expectedResponse =
                    new PageResponse<>(
                            List.of(messageDto),
                            0,
                            1,
                            null
                    );

            Slice<Message> messages =
                    new SliceImpl<>(List.of(message));

            given(channelRepository.existsById(channelId))
                    .willReturn(true);

            given(messageRepository.findAllByChannelId(
                    eq(channelId),
                    any(Pageable.class)
            )).willReturn(messages);

            given(pageResponseMapper.fromSlice(
                    eq(messages),
                    org.mockito.ArgumentMatchers
                            .<Function<Message, MessageDto>>any()
            )).willReturn(expectedResponse);

            // when
            PageResponse<MessageDto> result =
                    messageService.findAllByChannelId(channelId, 0);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(messageRepository)
                    .should()
                    .findAllByChannelId(
                            eq(channelId),
                            any(Pageable.class)
                    );

            then(pageResponseMapper)
                    .should()
                    .fromSlice(
                            eq(messages),
                            org.mockito.ArgumentMatchers
                                    .<Function<Message, MessageDto>>any()
                    );
        }

        @Test
        @DisplayName("존재하지 않는 채널이면 목록 조회에 실패한다")
        void findAllByChannelIdFailWhenChannelNotFound() {
            // given
            UUID channelId = UUID.randomUUID();

            given(channelRepository.existsById(channelId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(
                    () -> messageService.findAllByChannelId(channelId, 0)
            ).isInstanceOf(ChannelNotFoundException.class);

            then(messageRepository)
                    .shouldHaveNoInteractions();

            then(pageResponseMapper)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("페이지 번호가 음수이면 목록 조회에 실패한다")
        void findAllByChannelIdFailWhenPageIsNegative() {
            // given
            UUID channelId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(
                    () -> messageService.findAllByChannelId(channelId, -1)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("페이지 번호는 0 이상이어야 합니다.");

            then(channelRepository)
                    .shouldHaveNoInteractions();

            then(messageRepository)
                    .shouldHaveNoInteractions();

            then(pageResponseMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("메시지 수정")
    class UpdateTest {

        @Test
        @DisplayName("메시지 수정에 성공한다")
        void updateSuccess() {
            // given
            UUID messageId = UUID.randomUUID();

            UpdateMessageRequest request =
                    org.mockito.Mockito.mock(UpdateMessageRequest.class);

            Message message =
                    org.mockito.Mockito.mock(Message.class);

            MessageDto expectedResponse =
                    org.mockito.Mockito.mock(MessageDto.class);

            given(request.getContent())
                    .willReturn("수정된 메시지");

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            given(message.getAttachments())
                    .willReturn(List.of());

            given(messageMapper.toDto(message))
                    .willReturn(expectedResponse);

            // when
            MessageDto result =
                    messageService.update(messageId, request);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(message)
                    .should()
                    .update(
                            "수정된 메시지",
                            List.of()
                    );

            then(messageMapper)
                    .should()
                    .toDto(message);
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 수정하면 실패한다")
        void updateFailWhenMessageNotFound() {
            // given
            UUID messageId = UUID.randomUUID();

            UpdateMessageRequest request =
                    org.mockito.Mockito.mock(UpdateMessageRequest.class);

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> messageService.update(messageId, request)
            ).isInstanceOf(MessageNotFoundException.class);

            then(messageMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("메시지 삭제")
    class DeleteTest {

        @Test
        @DisplayName("메시지 삭제에 성공한다")
        void deleteSuccess() {
            // given
            UUID messageId = UUID.randomUUID();

            Message message =
                    org.mockito.Mockito.mock(Message.class);

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.of(message));

            // when
            messageService.delete(messageId);

            // then
            then(messageRepository)
                    .should()
                    .delete(message);
        }

        @Test
        @DisplayName("존재하지 않는 메시지를 삭제하면 실패한다")
        void deleteFailWhenMessageNotFound() {
            // given
            UUID messageId = UUID.randomUUID();

            given(messageRepository.findById(messageId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> messageService.delete(messageId)
            ).isInstanceOf(MessageNotFoundException.class);

            then(messageRepository)
                    .should(never())
                    .delete(any(Message.class));
        }
    }
}