package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.projection.MessageProjection;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MapStructMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.SessionService;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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

    @Mock
    SessionService sessionService;

    @Spy
    MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    @InjectMocks
    BasicMessageService messageService;

    private MessageDto messageDto(UUID id, UUID channelId, UUID authorId, String content){
        return new MessageDto(id, Instant.now(),Instant.now(),content,null,null,null);
    }



    @Nested
    @DisplayName("Message Create Test")
    class CreateTests {

        private MessageCreateRequest messageCreateRequest(String content){
            return new MessageCreateRequest(content,UUID.randomUUID(),UUID.randomUUID());
        }

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

            when(user.getId()).thenReturn(UUID.randomUUID());

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

            given(sessionService.userOnline(nullable(String.class))).willReturn(false);

            // when

            // then
            MessageDto res = messageService.createMessage(request, List.of());

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
            assertThatThrownBy(() -> messageService.createMessage(request, List.of()))
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

        private MessageProjection getProjection(Message message){
            return new MessageProjection(
                    message.getId(),
                    message.getCreatedAt(),
                    message.getUpdatedAt(),
                    message.getContent(),
                    message.getChannel().getId(),
                    message.getAuthor().getId(),
                    message.getAttachment().stream()
                            .map(BinaryContent::getId)
                            .filter(Objects::nonNull)
                            .toList()
            );
        }

        private BinaryContentDto getContentDto(BinaryContent content){
            return new BinaryContentDto(
                    content.getId(),
                    content.getFileName(),
                    content.getSize(),
                    content.getContentType(),
                    "dummy".getBytes(StandardCharsets.UTF_8)
            );
        }

        private Channel channel(UUID id){
            Channel channel = new Channel(
                    null,
                    null,
                    ChannelType.PRIVATE
            );

            ReflectionTestUtils.setField(channel,"id",id);

            return channel;
        }

        private User user(UUID id, String name){
            User user = new User(
                    name,
                    name + "@email.com",
                    "password",
                    null,
                    Role.USER
            );

            ReflectionTestUtils.setField(user,"id",id);

            return user;
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
            UUID userId = UUID.randomUUID();
            UUID messageId = UUID.randomUUID();
            // 3. cursor
            Instant cursor = Instant.now();

            // object for check logic
            // 1. message
            Channel channel = channel(channelId);
            User user = user(userId,"ksk");
            Message message = new Message("beforeContents", channel, user, List.of());
            // 2. messageDto
            MessageDto dto = messageDto(messageId,channelId,null,null);
            // 3. Slice
            Slice<UUID> messageIdList = new SliceImpl<>(
                    List.of(messageId),
                    pageable,
                    false
            );



            // when
            // 1. query target message ids
            given(messageRepository.findMessageIdsBuChannelIdWithCursor(
                    any(UUID.class),
                    any(Pageable.class),
                    any(Instant.class)
            )).willReturn(messageIdList);
            // 2.1. query message info
            given(messageRepository.getMessageProjectionFromIdList(
                    messageIdList.getContent()
            )).willReturn(
                    messageIdList.getContent().stream().collect(
                            Collectors.toMap(
                                    id -> id,
                                    t -> getProjection(message)
                            )
                    )
            );
            // 2.2. get user info from Id
            given(userRepository.getUsersFromIds(
                    any())
            ).willReturn(
                    Map.of(
                            userId,
                            new UserProjection(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getEmail(),
                                    user.getPassword(),
                                    user.getRole(),
                                    null
                            )
                    )
            );
            // 2.3 query message info and user and contents
            given(binaryContentRepository.getBinaryContentsInIdList(
                    any()
            )).willReturn(
                    new HashMap<>()
            );


            PageResponse<MessageDto> result = messageService.findallByChannelIdWithCursor(
                    channelId,
                    pageable,
                    cursor
            );



//            assertThat(result.content()).extracting(
//                    MessageDto::id
//            ).isEqualTo(messageId);

            verify(messageRepository,times(1))
                    .findMessageIdsBuChannelIdWithCursor(any(),any(),any());

            verify(messageRepository,times(1))
                    .getMessageProjectionFromIdList(any());

            verify(userRepository,times(1))
                    .getUsersFromIds(any());

            verify(binaryContentRepository,times(2))
                    .getBinaryContentsInIdList(any());

        }



    }



}
