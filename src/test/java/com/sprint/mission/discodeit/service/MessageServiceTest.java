package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Message Service Test")
public class MessageServiceTest {
    @Mock
    MessageRepository messageRepository;
    @Mock UserRepository userRepository;
    @Mock ChannelRepository channelRepository;
    @Mock BinaryContentRepository binaryContentRepository;
    @Mock BinaryContentStorage binaryContentStorage;
    @Mock PageResponseMapper pageResponseMapper;
    @Mock MapStructMapper mapStructMapper;

    @InjectMocks
    BasicMessageService messageService;

    private MessageDto messageDto(UUID id, UUID channelId, UUID authorId, String content){
        return new MessageDto(id, Instant.now(),Instant.now(),content,null,null,null);
    }

    private MessageCreateRequest messageCreateRequest(String content){
        return new MessageCreateRequest(content,UUID.randomUUID(),UUID.randomUUID());
    }

    @Nested
    @DisplayName("Message Create Test")
    class CreateTests {

        @Test
        @DisplayName("success")
        void success() {
            // Logic
            // 1. check user exist
            // 2. check channel exist
            // 2.e. make List of BinaryContent
            // 3. save Message to messageRepository
            // 4. convert to Dto (MessageDto, UserDto, BinaryContentDto)

            // given
            // MessageCreateRequest info
            String messageContent = "messageContent";
            MessageCreateRequest request = messageCreateRequest(messageContent);
            // Message info
            UUID messageId = UUID.randomUUID();
            Channel channel = mock(Channel.class);
            User user = mock(User.class);

            // 1.
            given(userRepository.findById(request.authorId())).willReturn(Optional.of(user));
            // 2.
            given(channelRepository.findById(request.channelId())).willReturn(Optional.of(channel));
            // 3.
            given(messageRepository.save(any(Message.class))).willAnswer( i -> {
                Message message = i.getArgument(0);
                ReflectionTestUtils.setField(message,"id",messageId);
                return message;
            });
            // 4.1. MessageDto
            given(mapStructMapper.toDto(
                    any(Message.class),
                    nullable(UserDto.class),
                    nullable(List.class)))  // how to fix this unsafe insert?
                    .willReturn(messageDto(messageId,request.channelId(),request.authorId(),request.content()));
            // 4.2. BinaryContentDto
            given(mapStructMapper.toDto(nullable(BinaryContent.class),eq(null))).willReturn(null);
            // 4.3. UserDto
            given(mapStructMapper.toDto(any(User.class),nullable(BinaryContentDto.class),nullable(Boolean.class))).willReturn(null);

            // when

            // then
            MessageDto res = messageService.createMessage(request, Optional.empty());

            assertThat(res.id()).isEqualTo(messageId);
        }
        
        @Test
        @DisplayName("메세지 생성 실패")
        void fail() {
            // logic
            // 1.the user (or channel) not existed.

            // given
            MessageCreateRequest request = messageCreateRequest("messageContent");
        
            // when
            given(userRepository.findById(request.authorId())).willReturn(Optional.empty());

            // then
            assertThatThrownBy(() -> messageService.createMessage(request, Optional.empty()))
                    .isInstanceOf(UserNotFoundException.class);
            
        }


    }


    @Nested
    @DisplayName("Message Update Test")
    class UpdateTests {


        @Test
        @DisplayName("success")
        void success() {
            // Logic
            // 1. check message exist
            // 2. save message
            // 3. make to dto

            // given
            UUID messageId = UUID.randomUUID();
            String newContent = "newContent";
            MessageUpdateRequest request = new MessageUpdateRequest(newContent);
            Message message = new Message("beforeContents",mock(Channel.class),mock(User.class),List.of());

            // when
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
            given(messageRepository.save(any(Message.class))).willReturn(message);

            given(mapStructMapper.toDto(
                    any(Message.class),
                    nullable(UserDto.class),
                    nullable(List.class)))  // how to fix this unsafe insert?
                    .willReturn(messageDto(messageId,null,null,newContent));
            // 4.2. BinaryContentDto
            given(mapStructMapper.toDto(nullable(BinaryContent.class),eq(null))).willReturn(null);
            // 4.3. UserDto
            given(mapStructMapper.toDto(any(User.class),nullable(BinaryContentDto.class),nullable(Boolean.class))).willReturn(null);


            // then
            MessageDto dto = messageService.updateMessageData(messageId,request);

            assertThat(dto.content()).isEqualTo(message.getContent());
        }

        @Test
        @DisplayName("update fail - Message not exsited")
        void fail() {
            // given
            UUID messageId = UUID.randomUUID();
            given(messageRepository.findById(messageId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> messageService.updateMessageData(messageId,null))
                    .isInstanceOf(MessageNotFoundException.class);

            // then


        }
    }

    @Nested
    @DisplayName("Message Delete Test")
    class DeleteTests {
        @Test
        @DisplayName("success")
        void success() {
            // Logic
            // 1. check Message from UUID
            // 1.+. if has binarycontent. delete bc
            // 2. delete message

            // given
            UUID messageId = UUID.randomUUID();
            Message message = new Message("beforeContents",mock(Channel.class),mock(User.class),List.of());
            // when
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
            // then
            messageService.deleteMessage(messageId);

            verify(messageRepository).delete(message);
        }

        @Test
        @DisplayName("delete fail - Message not exsited")
        void fail() {
            // given
            UUID messageId = UUID.randomUUID();
            given(messageRepository.findById(messageId)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> messageService.deleteMessage(messageId))
                    .isInstanceOf(MessageNotFoundException.class);

            // then
        }

    }

    @Nested
    @DisplayName("find Message by Channel")
    class FindByChannel {

        @Test
        @DisplayName("find success")
        void success() {
            // given

            // input param set
            // 1. pageable
            Pageable pageable = PageRequest.of(0, 10);
            // 2. channel id
            UUID channelId = UUID.randomUUID();

            // response data set
            // 1. message
            Channel channel = mock(Channel.class);
            User user = mock(User.class);
            UUID messageId = UUID.randomUUID();
            Message  message = new Message("beforeContents", channel, user, List.of());
            // 2. messageDto
            MessageDto dto = messageDto(messageId,channelId,null,null);
            // 3. Slice
            Slice<Message> slice = new SliceImpl<>(List.of(message));

            // when
            // 1. get slice of message from msg repo
            given(messageRepository.findByChannelIdForMessageDto(any(UUID.class),nullable(Pageable.class)))
                    .willReturn(slice);
            // 2. get message dto from message and user dto and attribute dto
            given(mapStructMapper.toDto(any(Message.class),nullable(UserDto.class),nullable(List.class)))
                    .willReturn(dto);

            // convert to dto from user, bc
            // 2.1. BinaryContentDto
            given(mapStructMapper.toDto(nullable(BinaryContent.class),eq(null))).willReturn(null);
            // 2.2. UserDto
            given(mapStructMapper.toDto(any(User.class),nullable(BinaryContentDto.class),nullable(Boolean.class))).willReturn(null);


            // then
            // 1. get pageres from slice<MessageDto>
            assertThat(messageService.findallByChannelId(channelId,null))
                    .isEqualTo(
                           pageResponseMapper.fromSlice(new SliceImpl<>(List.of(dto)))
                    );

        }

        @Test
        @DisplayName("find with cursor")
        void successWithCursor() {
            // given
            // param
            // 1. pageable
            Pageable pageable = PageRequest.of(0, 10);
            // 2. channel id
            UUID channelId = UUID.randomUUID();
            // 3. cursor
            Instant cursor = Instant.now();

            // object for check logic
            // 1. message
            Channel channel = mock(Channel.class);
            User user = mock(User.class);
            UUID messageId = UUID.randomUUID();
            Message  message = new Message("beforeContents", channel, user, List.of());
            // 2. messageDto
            MessageDto dto = messageDto(messageId,channelId,null,null);
            // 3. Slice
            Slice<Message> slice = new SliceImpl<>(List.of(message));



            // when
            // message repository will return slice of message
            given(messageRepository.findByChannelWithCursor(
                    any(UUID.class),
                    any(Pageable.class),
                    any(Instant.class)
            )).willReturn(slice);

            // convert message to messageDto
            // 2. get message dto from message and user dto and attribute dto
            given(mapStructMapper.toDto(any(Message.class),nullable(UserDto.class),nullable(List.class)))
                    .willReturn(dto);

            // convert to dto from user, bc
            // 2.1. BinaryContentDto
            given(mapStructMapper.toDto(nullable(BinaryContent.class),eq(null))).willReturn(null);
            // 2.2. UserDto
            given(mapStructMapper.toDto(any(User.class),nullable(BinaryContentDto.class),nullable(Boolean.class))).willReturn(null);

            // then
            assertThat(messageService.findallByChannelIdWithCursor(
                    channelId,
                    pageable,
                    cursor
            )).isEqualTo(
                    pageResponseMapper.fromSliceWithCursor(
                            slice,
                            cursor
                    )
            );

        }



    }



}
