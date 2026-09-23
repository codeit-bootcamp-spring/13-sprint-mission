package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.message.MessageCreateCommand;
import com.sprint.mission.discodeit.dto.command.message.MessageUpdateCommand;
import com.sprint.mission.discodeit.dto.repository.MessagePagingCondition;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.MessageFile;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageInvalidPagingConditionException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.ChannelReader;
import com.sprint.mission.discodeit.service.basic.MessageFileService;
import com.sprint.mission.discodeit.service.basic.UserReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    BasicMessageService messageService;

    @Mock
    MessageRepository messageRepository;

    @Mock
    MessageFileService messageFileService;

    @Mock
    UserReader userReader;

    @Mock
    ChannelReader channelReader;

    @Mock
    MessageMapper messageMapper;

    @Mock
    PageResponseMapper<MessageDto> messageDtoPageResponseMapper;

    @Nested
    @DisplayName("메시지 생성")
    class SaveMessageTest {

        @Test
        @DisplayName("메시지 생성 성공")
        void save_returnsMessageDto_whenCommandIsValid() {
            // given
            // 메시지 생성 요청 command와 저장 후 부여될 messageId를 준비한다.
            // command에는 메시지 내용, 작성자 id, 채널 id가 들어 있다.
            UUID messageId = UUID.randomUUID();
            MessageCreateCommand command = createMessageCommand();

            // BasicMessageService.save(...)는 command의 id 값으로 실제 User와 Channel 엔티티를 조회한다.
            // 이 테스트의 관심사는 조회 자체가 아니라 조회된 객체로 Message가 만들어지는지이므로 mock으로 충분하다.
            User author = mock(User.class);
            Channel channel = mock(Channel.class);

            // 첨부 파일이 없는 기본 메시지 생성 케이스다.
            // multipartFiles는 요청으로 들어온 파일 목록이고, messageFiles는 저장 후 반환되는 메시지 파일 엔티티 목록이다.
            List<MultipartFile> multipartFiles = List.of();
            List<MessageFile> messageFiles = List.of();
            List<BinaryContentDto> binaryContentDtos = List.of();

            // mapper가 반환할 최종 DTO를 준비한다.
            // 실제 DTO 변환은 MessageMapper의 책임이므로, 서비스 테스트에서는 mapper가 이 값을 반환한다고 가정한다.
            MessageDto messageDto = createMessageDto(messageId, command, binaryContentDtos);

            // save(...)는 먼저 채널을 조회하고, 그 다음 작성자를 조회한다.
            given(channelReader.getChannel(command.channelId())).willReturn(channel);
            given(userReader.getUser(command.userId())).willReturn(author);

            // messageRepository는 mock이므로 실제 DB 저장이나 id 생성을 하지 않는다.
            // 서비스는 저장된 Message를 이후 파일 저장과 DTO 변환에 사용하므로,
            // 테스트에서는 저장 요청으로 들어온 Message에 id를 넣고 그대로 반환하게 한다.
            given(messageRepository.save(any(Message.class))).willAnswer(inv -> {
                Message message = inv.getArgument(0);
                ReflectionTestUtils.setField(message, "id", messageId);
                return message;
            });

            // 첨부 파일 저장 결과로 빈 MessageFile 목록이 반환되는 상황을 만든다.
            given(messageFileService.save(any(Message.class), eq(multipartFiles))).willReturn(messageFiles);

            // 저장된 메시지와 저장된 파일 목록을 DTO로 변환하면 준비한 messageDto가 반환되도록 한다.
            given(messageMapper.toDto(any(Message.class), eq(messageFiles))).willReturn(messageDto);

            // when
            // 메시지 생성 로직을 실행한다.
            MessageDto result = messageService.save(command, multipartFiles);

            // then
            // 서비스는 mapper가 만들어준 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(messageDto);

            // 주요 협력 객체들이 서비스 구현 순서대로 호출됐는지 확인한다.
            // 메시지 생성 성공 흐름은 다음 순서를 갖는다.
            // 1. 채널 조회
            // 2. 작성자 조회
            // 3. Message 엔티티 생성 후 저장
            // 4. 첨부 파일 저장
            // 5. MessageDto 변환
            InOrder inOrder = inOrder(channelReader, userReader, messageRepository, messageFileService, messageMapper);
            inOrder.verify(channelReader).getChannel(command.channelId());
            inOrder.verify(userReader).getUser(command.userId());

            // 저장 요청으로 만들어진 Message를 캡처해서 command, channel, author가 잘 반영됐는지 확인한다.
            // captor.capture()는 단독으로 호출하면 안 되고, verify(...) 안에서 사용해야 한다.
            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            inOrder.verify(messageRepository).save(captor.capture());
            Message message = captor.getValue();

            assertThat(message.getContent()).isEqualTo(command.content());
            assertThat(message.getChannel()).isEqualTo(channel);
            assertThat(message.getAuthor()).isEqualTo(author);
            assertThat(message.getMessageFiles()).isEmpty();
            assertThat(message.getId()).isEqualTo(messageId);

            // 저장된 Message가 첨부 파일 저장과 DTO 변환에 그대로 사용됐는지 확인한다.
            inOrder.verify(messageFileService).save(message, multipartFiles);
            inOrder.verify(messageMapper).toDto(message, messageFiles);

            // 위에서 검증한 생성 흐름 외에 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(channelReader, userReader, messageRepository, messageFileService, messageMapper);

            // 메시지 생성은 페이지 응답을 만들지 않으므로 PageResponseMapper는 호출되면 안 된다.
            verifyNoInteractions(messageDtoPageResponseMapper);
        }

        @Test
        @DisplayName("메시지 생성 성공 - 첨부 파일이 있음")
        void save_returnsMessageDtoWithAttachments_whenFilesExist() {
            // given
            // 메시지 생성 요청과 저장 후 부여될 messageId를 준비한다.
            // 이 테스트는 첨부 파일이 전달된 메시지 생성 성공 케이스다.
            UUID messageId = UUID.randomUUID();
            MessageCreateCommand messageCommand = createMessageCommand();

            // save(...)는 command의 channelId/userId로 Channel과 User를 조회한다.
            // 조회된 객체로 Message 엔티티가 만들어지는지 확인하기 위해 mock 객체를 사용한다.
            Channel channel = mock(Channel.class);
            User author = mock(User.class);

            // 요청으로 들어온 첨부 파일 목록을 준비한다.
            // MessageFileService.save(...)는 MultipartFile을 저장하고 MessageFile 목록을 반환한다.
            MockMultipartFile file = createMockMultipartFile();
            List<MultipartFile> multipartFiles = List.of(file);

            // 첨부 파일 저장 후 반환되는 MessageFile 엔티티 목록과,
            // mapper가 최종 DTO에 넣어줄 첨부 파일 DTO 목록을 준비한다.
            List<MessageFile> messageFiles = List.of(mock(MessageFile.class));
            List<BinaryContentDto> binaryContentDtos = List.of(mock(BinaryContentDto.class));

            // mapper가 반환할 최종 DTO를 준비한다.
            // 실제 MessageFile -> BinaryContentDto 변환은 mapper 책임이므로 서비스 테스트에서는 mapper 결과로 가정한다.
            MessageDto messageDto = createMessageDto(messageId, messageCommand, binaryContentDtos);

            // 메시지 생성은 채널 조회 후 작성자 조회 순서로 진행된다.
            given(channelReader.getChannel(messageCommand.channelId())).willReturn(channel);
            given(userReader.getUser(messageCommand.userId())).willReturn(author);

            // messageRepository는 mock이라 실제 DB 저장과 id 생성을 하지 않는다.
            // 이후 파일 저장과 mapper 변환에서 저장된 Message를 사용하므로 id를 직접 넣어 반환한다.
            given(messageRepository.save(any(Message.class))).willAnswer(inv -> {
                Message message = inv.getArgument(0);
                ReflectionTestUtils.setField(message, "id", messageId);
                return message;
            });

            // 첨부 파일 저장 결과로 MessageFile 목록이 반환되는 상황을 만든다.
            given(messageFileService.save(any(Message.class), eq(multipartFiles))).willReturn(messageFiles);

            // 저장된 메시지와 저장된 첨부 파일 목록을 DTO로 변환하면 준비한 messageDto가 반환되도록 한다.
            given(messageMapper.toDto(any(Message.class), eq(messageFiles))).willReturn(messageDto);

            // when
            // 첨부 파일이 있는 메시지 생성 로직을 실행한다.
            MessageDto result = messageService.save(messageCommand, multipartFiles);

            // then
            // 서비스는 mapper가 만들어준 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(messageDto);
            assertThat(result.attachments()).isEqualTo(binaryContentDtos);
            assertThat(result.attachments()).isNotEmpty();

            // 주요 협력 객체들이 서비스 구현 순서대로 호출됐는지 확인한다.
            // 메시지 생성 성공 흐름은 채널 조회 -> 작성자 조회 -> 메시지 저장 -> 첨부 파일 저장 -> DTO 변환 순서다.
            InOrder inOrder = inOrder(channelReader, userReader, messageRepository, messageFileService, messageMapper);
            inOrder.verify(channelReader).getChannel(messageCommand.channelId());
            inOrder.verify(userReader).getUser(messageCommand.userId());

            // 저장 요청으로 만들어진 Message를 캡처해서 command, channel, author가 잘 반영됐는지 확인한다.
            ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
            inOrder.verify(messageRepository).save(captor.capture());
            Message message = captor.getValue();

            assertThat(message.getId()).isEqualTo(messageId);
            assertThat(message.getContent()).isEqualTo(messageCommand.content());
            assertThat(message.getChannel()).isEqualTo(channel);
            assertThat(message.getAuthor()).isEqualTo(author);

            // MessageFileService.save(...)는 첨부 파일을 별도로 저장해서 messageFiles를 반환한다.
            // BasicMessageService.save(...)가 이 반환값을 Message 엔티티의 messageFiles 컬렉션에 직접 추가하지는 않는다.
            // 따라서 Message 내부 컬렉션이 아니라, messageFiles가 mapper에 전달되는지를 검증해야 한다.
            assertThat(message.getMessageFiles()).isEmpty();

            // 저장된 Message와 요청 파일 목록이 첨부 파일 저장에 사용됐는지 확인한다.
            inOrder.verify(messageFileService).save(message, multipartFiles);

            // 첨부 파일 저장 결과인 messageFiles가 DTO 변환에 그대로 전달됐는지 확인한다.
            inOrder.verify(messageMapper).toDto(message, messageFiles);

            // 위에서 검증한 생성 흐름 외에 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(channelReader, userReader, messageRepository, messageFileService, messageMapper);

            // 메시지 생성은 페이지 응답을 만들지 않으므로 PageResponseMapper는 호출되면 안 된다.
            verifyNoInteractions(messageDtoPageResponseMapper);
        }

        @Test
        @DisplayName("메시지 생성 실패 - 존재하지 않는 채널")
        void save_throwsChannelNotFoundException_whenChannelDoesNotExist() {
            // given
            // 메시지 생성 요청과 첨부 파일 목록을 준비한다.
            // 이 테스트는 command에 들어 있는 channelId에 해당하는 채널이 존재하지 않는 실패 케이스다.
            MessageCreateCommand messageCommand = createMessageCommand();
            List<MultipartFile> multipartFiles = List.of();

            // BasicMessageService.save(...)는 가장 먼저 channelReader.getChannel(...)로 채널을 조회한다.
            // 실제 ChannelReader.getChannel(...)은 채널을 찾지 못하면 null을 반환하지 않고 ChannelNotFoundException을 던진다.
            // 따라서 mock도 조회한 channelId가 담긴 예외 인스턴스를 던지도록 설정한다.
            given(channelReader.getChannel(messageCommand.channelId()))
                    .willThrow(new ChannelNotFoundException(messageCommand.channelId()));

            // when & then
            // 채널을 찾지 못하면 작성자 조회나 메시지 저장을 진행하지 않고 ChannelNotFoundException이 발생해야 한다.
            assertThatThrownBy(() -> messageService.save(messageCommand, multipartFiles))
                    .isInstanceOf(ChannelNotFoundException.class);

            // 메시지 생성은 채널 조회에서 시작하므로 channelReader 호출까지는 수행되어야 한다.
            verify(channelReader).getChannel(messageCommand.channelId());

            // channelReader에는 위에서 검증한 호출 외 추가 호출이 없어야 한다.
            verifyNoMoreInteractions(channelReader);

            // 채널 조회 단계에서 예외가 발생했으므로 이후 생성 성공 흐름은 실행되면 안 된다.
            // - userReader.getUser(...): 작성자 조회
            // - messageRepository.save(...): Message 엔티티 저장
            // - messageFileService.save(...): 첨부 파일 저장
            // - messageMapper.toDto(...): 저장된 메시지를 DTO로 변환
            verifyNoInteractions(userReader, messageRepository, messageFileService, messageMapper);

            // 메시지 생성 실패 흐름에서는 페이지 응답을 만들지 않으므로 PageResponseMapper도 호출되면 안 된다.
            verifyNoInteractions(messageDtoPageResponseMapper);
        }

        @Test
        @DisplayName("메시지 생성 실패 - 존재하지 않는 작성자")
        void save_throwsUserNotFoundException_whenAuthorDoesNotExist() {
            // given
            // 메시지 생성 요청과 첨부 파일 목록을 준비한다.
            // 이 테스트는 채널은 존재하지만 작성자가 존재하지 않는 실패 케이스다.
            MessageCreateCommand messageCommand = createMessageCommand();
            List<MultipartFile> multipartFiles = List.of();
            Channel channel = mock(Channel.class);

            // BasicMessageService.save(...)는 먼저 채널을 조회한 뒤 작성자를 조회한다.
            // 이 케이스에서는 채널 조회는 성공해야 작성자 조회 실패 흐름까지 도달할 수 있다.
            given(channelReader.getChannel(messageCommand.channelId())).willReturn(channel);

            // 실제 UserReader.getUser(...)는 사용자를 찾지 못하면 null을 반환하지 않고 UserNotFoundException을 던진다.
            // 따라서 mock도 null 반환이 아니라, 조회한 userId가 담긴 예외 인스턴스를 던지도록 설정한다.
            given(userReader.getUser(messageCommand.userId()))
                    .willThrow(new UserNotFoundException(messageCommand.userId()));

            // when & then
            // 작성자를 찾지 못하면 메시지를 저장하지 않고 UserNotFoundException이 발생해야 한다.
            assertThatThrownBy(() -> messageService.save(messageCommand, multipartFiles))
                    .isInstanceOf(UserNotFoundException.class);

            // 채널 조회 후 작성자 조회까지 진행됐는지 확인한다.
            verify(channelReader).getChannel(messageCommand.channelId());
            verify(userReader).getUser(messageCommand.userId());

            // channelReader와 userReader에는 위에서 검증한 호출 외 추가 호출이 없어야 한다.
            verifyNoMoreInteractions(channelReader, userReader);

            // 작성자 조회 단계에서 예외가 발생했으므로 이후 생성 성공 흐름은 실행되면 안 된다.
            // - messageRepository.save(...): Message 엔티티 저장
            // - messageFileService.save(...): 첨부 파일 저장
            // - messageMapper.toDto(...): 저장된 메시지를 DTO로 변환
            verifyNoInteractions(messageRepository, messageFileService, messageMapper);

            // 메시지 생성 실패 흐름에서는 페이지 응답을 만들지 않으므로 PageResponseMapper도 호출되면 안 된다.
            verifyNoInteractions(messageDtoPageResponseMapper);
        }
    }

    @Nested
    @DisplayName("메시지 단건 조회")
    class FindMessageTest {

        @Test
        @DisplayName("메시지 단건 조회 성공")
        void findById_returnsMessageDto_whenMessageExists() {
            // given
            // 조회 대상 메시지 ID를 준비한다.
            UUID messageId = UUID.randomUUID();

            // findById()의 관심사는 Message 엔티티 내부 값이 아니라
            // repository에서 조회된 Message가 첨부 파일과 함께 mapper로 전달되는지이다.
            // 따라서 Message는 단순 전달 객체 역할만 하므로 mock으로 충분하다.
            Message message = mock(Message.class);

            // 단건 조회 성공 흐름에서는 메시지에 연결된 첨부 파일 목록도 함께 조회한다.
            // MessageFile 내부 상태는 사용하지 않고 mapper로 전달하기만 하므로 mock으로 충분하다.
            List<MessageFile> messageFiles = List.of(mock(MessageFile.class));

            // mapper가 반환할 최종 DTO는 실제 record 객체로 만든다.
            // Mapper 자체는 mock이지만, DTO는 값 객체이므로 mock보다 실제 객체가 테스트 의도를 더 명확하게 만든다.
            MessageCreateCommand command = createMessageCommand();
            MessageDto messageDto = createMessageDto(messageId, command, List.of());

            // repository가 메시지를 찾은 상황을 만든다.
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

            // 메시지가 존재하면 해당 메시지 ID로 첨부 파일 목록을 조회해야 한다.
            given(messageFileService.findAllByMessageId(messageId)).willReturn(messageFiles);

            // 조회된 Message와 첨부 파일 목록이 mapper로 전달되면 기대 DTO를 반환하도록 설정한다.
            // 실제 필드 매핑이 정확한지는 MessageMapper 테스트에서 별도로 검증한다.
            given(messageMapper.toDto(message, messageFiles)).willReturn(messageDto);

            // when
            // 메시지 단건 조회를 실행한다.
            MessageDto result = messageService.findById(messageId);

            // then
            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(messageDto);

            // 단건 조회의 핵심 협력은 조회된 메시지 ID로 첨부 파일을 조회하고,
            // Message와 첨부 파일 목록을 mapper에 넘기는 것이다. 순서 자체는 별도 계약으로 고정하지 않는다.
            verify(messageRepository).findById(messageId);
            verify(messageFileService).findAllByMessageId(messageId);
            verify(messageMapper).toDto(message, messageFiles);

            // 위에서 검증한 단건 조회 성공 흐름 외에 추가 호출이 없는지 확인한다.
            // 추가 조회나 중복 mapper 호출은 이 서비스 메서드의 책임 범위를 벗어난다.
            verifyNoMoreInteractions(messageRepository, messageFileService, messageMapper);

            // findById()는 페이지 응답을 만들지 않고, 사용자/채널을 별도로 조회하지 않는다.
            // 따라서 PageResponseMapper, UserReader, ChannelReader는 이 흐름에서 호출되면 안 된다.
            verifyNoInteractions(messageDtoPageResponseMapper, userReader, channelReader);
        }
        @Test
        @DisplayName("메시지 단건 조회 실패 - 존재하지 않는 메시지")
        void findById_throwsMessageNotFoundException_whenMessageDoesNotExist() {
            // given
            // 조회할 messageId는 있지만, repository가 해당 메시지를 찾지 못하는 상황을 만든다.
            UUID messageId = UUID.randomUUID();

            // BasicMessageService.findById(...)는 내부적으로 messageRepository.findById(...)를 호출하고,
            // Optional.empty()이면 MessageNotFoundException을 던진다.
            given(messageRepository.findById(messageId)).willReturn(Optional.empty());

            // when & then
            // 메시지가 존재하지 않으면 첨부 파일 조회나 DTO 변환으로 넘어가지 않고 예외가 발생해야 한다.
            assertThatThrownBy(() -> messageService.findById(messageId))
                    .isInstanceOfSatisfying(MessageNotFoundException.class, exception ->
                            assertThat(exception.getDetails()).containsEntry("messageId", messageId)
                    );

            // messageId로 메시지 단건 조회를 시도했는지 확인한다.
            verify(messageRepository).findById(messageId);

            // repository에는 위에서 검증한 findById(...) 외 추가 호출이 없어야 한다.
            verifyNoMoreInteractions(messageRepository);

            // 메시지를 찾지 못한 시점에 예외가 발생하므로 후속 협력 객체는 호출되면 안 된다.
            // - messageFileService.findAllByMessageId(...): 메시지 첨부 파일 조회
            // - messageMapper.toDto(...): Message를 MessageDto로 변환
            verifyNoInteractions(messageFileService, messageMapper);

            // findById() 실패 흐름에서는 페이지 응답을 만들지 않고, 사용자/채널도 별도로 조회하지 않는다.
            // 따라서 PageResponseMapper, UserReader, ChannelReader는 호출되면 안 된다.
            verifyNoInteractions(messageDtoPageResponseMapper, userReader, channelReader);
        }
    }

    @Nested
    @DisplayName("채널별 메시지 목록 조회")
    class FindAllMessageTest {

        @Test
        @DisplayName("채널별 메시지 목록 조회 성공 - 다음 페이지가 있음")
        void findAllByChannelId_returnsPageResponseWithNextCursor_whenNextPageExists() {
            // given
            // 조회 대상 채널, 페이지 조건, cursor를 준비한다.
            // cursor는 메시지 목록 조회 기준값으로 repository에 그대로 전달되어야 한다.
            UUID channelId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            UUID cursor = UUID.randomUUID();
            MessagePagingCondition condition = new MessagePagingCondition(channelId, pageable, cursor);

            // 목록 조회 결과로 반환될 Message를 준비한다.
            // 서비스는 Message.getId()로 첨부 파일을 한 번에 조회하고, 다시 Message별 첨부 파일 목록을 매칭한다.
            UUID messageId = UUID.randomUUID();
            Message message = mock(Message.class);
            given(message.getId()).willReturn(messageId);

            List<Message> messages = List.of(message);

            // 다음 페이지가 있음을 검증해야 하므로 hasNext=true인 Slice를 준비한다.
            Slice<Message> sliceMessage = createMessageSlice(messages, pageable, true);
            List<UUID> messageIds = List.of(messageId);

            // MessageFile은 MessageFile.getMessageId() 기준으로 그룹핑된다.
            // 이 값을 stub 하지 않으면 mock의 기본값 null로 그룹핑되어, 서비스가 messageId로 첨부 파일을 찾지 못한다.
            MessageFile messageFile = mock(MessageFile.class);
            given(messageFile.getMessageId()).willReturn(messageId);
            List<MessageFile> messageFiles = List.of(messageFile);

            // nextCursor는 다음 페이지가 있을 때 응답된 마지막 메시지 ID 문자열로 계산된다.
            // DTO는 값 객체이므로 mock 대신 실제 record로 만든다.
            OffsetDateTime messageCreatedAt = OffsetDateTime.now();
            String nextCursor = messageId.toString();
            MessageDto messageDto = createMessageDto(
                    messageId,
                    messageCreatedAt,
                    "messageContent",
                    channelId,
                    UUID.randomUUID(),
                    List.of()
            );

            // PageResponse 생성 자체는 PageResponseMapper의 책임이다.
            // 서비스 테스트에서는 mapper가 반환한 응답을 그대로 반환하는지 검증한다.
            PageResponse<MessageDto> pageResponse = createPageResponse(messageDto, nextCursor, pageable, true);

            // repository가 다음 페이지가 있는 메시지 Slice를 반환하는 상황을 만든다.
            given(messageRepository.findAllByCondition(condition))
                    .willReturn(sliceMessage);

            // 서비스는 조회된 메시지 ID 목록으로 첨부 파일을 한 번에 조회한다.
            given(messageFileService.findAllByMessageIds(messageIds)).willReturn(messageFiles);

            // 그룹핑된 첨부 파일 목록이 Message와 함께 mapper에 전달되면 DTO를 반환하도록 설정한다.
            given(messageMapper.toDto(message, messageFiles)).willReturn(messageDto);

            // 서비스 내부에서 sliceMessage.map(...)으로 새로운 Slice<MessageDto>가 만들어진다.
            // 따라서 테스트에서 미리 만든 Slice 인스턴스로 stubbing하지 말고 any()로 받고,
            // 실제 전달된 Slice는 아래 ArgumentCaptor로 검증한다.
            given(messageDtoPageResponseMapper.fromSlice(any(), eq(nextCursor)))
                    .willReturn(pageResponse);

            // when
            // 채널별 메시지 목록을 조회한다.
            PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, pageable, cursor);

            // then
            // 서비스는 PageResponseMapper가 만든 응답을 그대로 반환해야 한다.
            assertThat(result).isEqualTo(pageResponse);

            InOrder inOrder = inOrder(messageRepository, messageFileService, messageMapper, messageDtoPageResponseMapper);

            // 1. 채널 ID, 페이지 조건, cursor를 담은 condition으로 메시지 Slice를 조회한다.
            inOrder.verify(messageRepository).findAllByCondition(condition);

            // 2. 조회된 Message ID 목록으로 첨부 파일을 한 번에 조회한다.
            inOrder.verify(messageFileService).findAllByMessageIds(messageIds);

            // 3. Message별로 매칭된 첨부 파일 목록을 함께 넘겨 DTO로 변환한다.
            inOrder.verify(messageMapper).toDto(message, messageFiles);

            // 4. DTO Slice와 계산된 nextCursor를 PageResponseMapper에 넘긴다.
            // nextCursor 계산은 응답된 마지막 Message id 값을 통해 검증한다.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Slice<MessageDto>> sliceCaptor = ArgumentCaptor.forClass(Slice.class);
            inOrder.verify(messageDtoPageResponseMapper).fromSlice(sliceCaptor.capture(), eq(nextCursor));

            Slice<MessageDto> capturedDtoSlice = sliceCaptor.getValue();
            assertThat(capturedDtoSlice.getContent()).containsExactly(messageDto);
            assertThat(capturedDtoSlice.hasNext()).isTrue();
            assertThat(capturedDtoSlice.getSize()).isEqualTo(pageable.getPageSize());

            // 위에서 검증한 목록 조회 성공 흐름 외에 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(messageRepository, messageFileService, messageMapper, messageDtoPageResponseMapper);

            // 채널별 메시지 목록 조회는 사용자/채널 엔티티를 별도로 조회하지 않는다.
            // channelId는 repository 조회 조건으로만 사용되므로 UserReader, ChannelReader는 호출되면 안 된다.
            verifyNoInteractions(userReader, channelReader);
        }
        @Test
        @DisplayName("채널별 메시지 목록 조회 성공 - 다음 페이지가 없음")
        void findAllByChannelId_returnsPageResponseWithNullNextCursor_whenNextPageDoesNotExist() {
            // given
            // 조회 대상 채널, 페이지 조건, cursor를 준비한다.
            // cursor는 repository의 메시지 목록 조회 조건으로 그대로 전달되어야 한다.
            UUID channelId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            UUID cursor = UUID.randomUUID();
            MessagePagingCondition condition = new MessagePagingCondition(channelId, pageable, cursor);

            // 첫 번째 메시지를 준비한다.
            // 서비스는 Message.getId()로 메시지 ID 목록을 만들기 때문에 id 값을 반드시 stub 해야 한다.
            UUID firstMessageId = UUID.randomUUID();
            Message firstMessage = mock(Message.class);
            given(firstMessage.getId()).willReturn(firstMessageId);

            // 두 번째 메시지도 함께 준비해서 여러 메시지가 순서대로 DTO 변환되는지 확인한다.
            UUID secondMessageId = UUID.randomUUID();
            Message secondMessage = mock(Message.class);
            given(secondMessage.getId()).willReturn(secondMessageId);

            List<UUID> messageIds = List.of(firstMessageId, secondMessageId);
            List<Message> messages = List.of(firstMessage, secondMessage);

            // 다음 페이지가 없는 상황을 만들어야 하므로 hasNext=false인 Slice를 반환하도록 준비한다.
            Slice<Message> sliceMessage = createMessageSlice(messages, pageable, false);

            // 첫 번째 메시지에만 첨부 파일이 있는 상황을 만든다.
            // MessageFile은 getMessageId() 기준으로 그룹핑되므로, 첫 번째 메시지 ID를 반환하도록 stub 한다.
            MessageFile firstMessageFile = mock(MessageFile.class);
            given(firstMessageFile.getMessageId()).willReturn(firstMessageId);
            List<MessageFile> messageFiles = List.of(firstMessageFile);

            // 서비스 내부에서는 messageId별로 그룹핑된 첨부 파일 목록을 mapper에 전달한다.
            // 첫 번째 메시지는 첨부 파일 1개, 두 번째 메시지는 첨부 파일이 없으므로 빈 리스트가 전달되어야 한다.
            List<MessageFile> firstMessageFiles = List.of(firstMessageFile);
            List<MessageFile> secondMessageFiles = List.of();

            // DTO 변환 결과는 실제 record 객체로 준비한다.
            // 다음 페이지가 없으면 DTO createdAt 값이 있더라도 nextCursor는 null이어야 한다.
            MessageDto firstMessageDto = createMessageDto(
                    firstMessageId,
                    OffsetDateTime.now().minusSeconds(10),
                    "firstMessage",
                    channelId,
                    UUID.randomUUID(),
                    List.of()
            );
            MessageDto secondMessageDto = createMessageDto(
                    secondMessageId,
                    OffsetDateTime.now(),
                    "secondMessage",
                    channelId,
                    UUID.randomUUID(),
                    List.of()
            );
            List<MessageDto> messageDtos = List.of(firstMessageDto, secondMessageDto);

            // 다음 페이지가 없으면 nextCursor는 null이어야 한다.
            // PageResponse 생성은 PageResponseMapper 책임이므로, 서비스는 이 응답을 그대로 반환하는지만 확인한다.
            PageResponse<MessageDto> pageResponse = createPageResponse(messageDtos, null, pageable, false);

            // repository가 다음 페이지가 없는 메시지 Slice를 반환하는 상황을 만든다.
            given(messageRepository.findAllByCondition(condition))
                    .willReturn(sliceMessage);

            // 서비스는 Slice에 담긴 메시지 ID 목록으로 첨부 파일을 한 번에 조회한다.
            given(messageFileService.findAllByMessageIds(messageIds))
                    .willReturn(messageFiles);

            // 첫 번째 메시지는 연결된 첨부 파일 목록과 함께 DTO로 변환되어야 한다.
            given(messageMapper.toDto(firstMessage, firstMessageFiles))
                    .willReturn(firstMessageDto);

            // 두 번째 메시지는 첨부 파일이 없으므로 빈 리스트와 함께 DTO로 변환되어야 한다.
            given(messageMapper.toDto(secondMessage, secondMessageFiles))
                    .willReturn(secondMessageDto);

            // 서비스 내부에서 Slice.map(...)으로 새로운 Slice<MessageDto>가 만들어진다.
            // 따라서 특정 Slice 인스턴스로 stubbing하지 않고 any()로 받고, null cursor는 isNull()로 의도를 드러낸다.
            given(messageDtoPageResponseMapper.fromSlice(any(), isNull()))
                    .willReturn(pageResponse);

            // when
            // 채널별 메시지 목록을 조회한다.
            PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, pageable, cursor);

            // then
            // 서비스는 PageResponseMapper가 만든 응답을 그대로 반환해야 한다.
            assertThat(result).isEqualTo(pageResponse);
            assertThat(result.nextCursor()).isNull();
            assertThat(result.hasNext()).isFalse();

            InOrder inOrder = inOrder(messageRepository, messageFileService, messageMapper, messageDtoPageResponseMapper);

            // 1. 채널 ID, 페이지 조건, cursor를 담은 condition으로 메시지 Slice를 조회한다.
            inOrder.verify(messageRepository).findAllByCondition(condition);

            // 2. 조회된 메시지 ID 목록으로 첨부 파일을 한 번에 조회한다.
            inOrder.verify(messageFileService).findAllByMessageIds(messageIds);

            // 3. 각 메시지는 자신에게 매칭된 첨부 파일 목록과 함께 DTO로 변환된다.
            inOrder.verify(messageMapper).toDto(firstMessage, firstMessageFiles);
            inOrder.verify(messageMapper).toDto(secondMessage, secondMessageFiles);

            // 4. 다음 페이지가 없으므로 PageResponseMapper에는 null nextCursor가 전달되어야 한다.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Slice<MessageDto>> sliceCaptor = ArgumentCaptor.forClass(Slice.class);
            inOrder.verify(messageDtoPageResponseMapper).fromSlice(sliceCaptor.capture(), isNull());

            // PageResponseMapper에 전달된 DTO Slice의 내용과 페이지 상태를 확인한다.
            Slice<MessageDto> capturedDtoSlice = sliceCaptor.getValue();
            assertThat(capturedDtoSlice.getContent()).containsExactlyElementsOf(messageDtos);
            assertThat(capturedDtoSlice.hasNext()).isFalse();
            assertThat(capturedDtoSlice.getSize()).isEqualTo(pageable.getPageSize());

            // hasNext=false이면 DTO에 createdAt 값이 있더라도 nextCursor는 null이어야 한다.
            // 이 값은 위 fromSlice(...) 검증에서 isNull()로 이미 확인한다.

            // 위에서 검증한 목록 조회 성공 흐름 외에 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(messageRepository, messageFileService, messageMapper, messageDtoPageResponseMapper);

            // 채널별 메시지 목록 조회는 사용자/채널 엔티티를 별도로 조회하지 않는다.
            // channelId는 repository 조회 조건으로만 사용되므로 UserReader, ChannelReader는 호출되면 안 된다.
            verifyNoInteractions(userReader, channelReader);
        }
        @Test
        @DisplayName("채널별 메시지 목록 조회 성공 - 조회 결과가 없음")
        void findAllByChannelId_returnsEmptyPageResponse_whenMessagesDoNotExist() {
            // given
            // 조회 대상 채널, 페이지 조건, cursor를 준비한다.
            // cursor는 repository의 메시지 목록 조회 조건으로 그대로 전달되어야 한다.
            UUID channelId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            UUID cursor = UUID.randomUUID();
            MessagePagingCondition condition = new MessagePagingCondition(channelId, pageable, cursor);

            // repository가 빈 Slice를 반환하는 상황을 만든다.
            // 조회된 메시지가 없으므로 hasNext도 false이다.
            List<Message> messages = List.of();
            SliceImpl<Message> messageSlice = createMessageSlice(messages, pageable, false);

            // 메시지가 없으면 DTO 목록도 비어 있어야 한다.
            // PageResponse 생성 자체는 PageResponseMapper의 책임이므로, 서비스는 이 응답을 그대로 반환하는지 검증한다.
            List<MessageDto> messageDtos = List.of();
            PageResponse<MessageDto> pageResponse = createPageResponse(messageDtos, null, pageable, false);

            // 채널별 메시지 조회 결과로 빈 Slice를 반환하도록 설정한다.
            given(messageRepository.findAllByCondition(condition))
                    .willReturn(messageSlice);

            // 빈 Slice에서는 nextCursor가 계산될 수 없으므로 PageResponseMapper에는 null이 전달되어야 한다.
            given(messageDtoPageResponseMapper.fromSlice(any(), isNull()))
                    .willReturn(pageResponse);

            // when
            // 채널별 메시지 목록을 조회한다.
            PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, pageable, cursor);

            // then
            // 서비스는 PageResponseMapper가 만든 응답을 그대로 반환해야 한다.
            assertThat(result).isEqualTo(pageResponse);
            assertThat(result.nextCursor()).isNull();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.content()).isEmpty();

            InOrder inOrder = inOrder(messageRepository, messageDtoPageResponseMapper);

            // 1. 채널 ID, 페이지 조건, cursor를 담은 condition으로 메시지 Slice를 조회한다.
            inOrder.verify(messageRepository).findAllByCondition(condition);

            // 2. 빈 DTO Slice와 null nextCursor를 PageResponseMapper에 넘긴다.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Slice<MessageDto>> sliceCaptor = ArgumentCaptor.forClass(Slice.class);
            inOrder.verify(messageDtoPageResponseMapper).fromSlice(sliceCaptor.capture(), isNull());

            // PageResponseMapper에 전달된 DTO Slice도 비어 있고, 다음 페이지가 없어야 한다.
            Slice<MessageDto> capturedDtoSlice = sliceCaptor.getValue();
            assertThat(capturedDtoSlice.getContent()).isEmpty();
            assertThat(capturedDtoSlice.hasNext()).isFalse();
            assertThat(capturedDtoSlice.getSize()).isEqualTo(pageable.getPageSize());

            // 조회된 메시지가 없으면 messageIds도 비어 있으므로 첨부 파일 조회를 수행하지 않는다.
            // 또한 변환할 Message가 없으므로 MessageMapper도 호출되면 안 된다.
            verifyNoInteractions(messageMapper, messageFileService);

            // 위에서 검증한 repository/PageResponseMapper 호출 외 추가 호출이 없어야 한다.
            verifyNoMoreInteractions(messageRepository, messageDtoPageResponseMapper);

            // 채널별 메시지 목록 조회는 사용자/채널 엔티티를 별도로 조회하지 않는다.
            // channelId는 repository 조회 조건으로만 사용되므로 UserReader, ChannelReader는 호출되면 안 된다.
            verifyNoInteractions(userReader, channelReader);
        }

        @Test
        @DisplayName("채널별 메시지 목록 조회 성공 - 첨부 파일이 없는 메시지는 빈 첨부 파일 목록으로 변환")
        void findAllByChannelId_mapsEmptyMessageFiles_whenMessageFilesDoNotExist() {
            // given
            // 조회 대상 채널, 페이지 조건, cursor를 준비한다.
            // cursor는 repository의 메시지 목록 조회 조건으로 그대로 전달되어야 한다.
            UUID channelId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            UUID cursor = UUID.randomUUID();
            MessagePagingCondition condition = new MessagePagingCondition(channelId, pageable, cursor);

            // 이 테스트의 핵심 시나리오는 "목록 조회 결과 전체에는 첨부 파일이 존재하지만,
            // 일부 메시지에는 매칭되는 첨부 파일이 없는 경우"이다.
            // 서비스는 메시지별 첨부 파일을 Map<messageId, List<MessageFile>>로 그룹핑한 뒤,
            // 해당 messageId가 Map에 없으면 mapper에 null이 아니라 빈 리스트를 넘겨야 한다.

            // 첫 번째 메시지를 준비한다.
            // 서비스는 Message.getId()로 메시지 ID 목록을 만들기 때문에 id 값을 반드시 stub 해야 한다.
            UUID firstMessageId = UUID.randomUUID();
            Message firstMessage = mock(Message.class);
            given(firstMessage.getId()).willReturn(firstMessageId);

            // 두 번째 메시지도 함께 준비해서 여러 메시지가 순서대로 DTO 변환되는지 확인한다.
            UUID secondMessageId = UUID.randomUUID();
            Message secondMessage = mock(Message.class);
            given(secondMessage.getId()).willReturn(secondMessageId);

            List<UUID> messageIds = List.of(firstMessageId, secondMessageId);
            List<Message> messages = List.of(firstMessage, secondMessage);

            // 다음 페이지가 없는 상황을 만들어야 하므로 hasNext=false인 Slice를 반환하도록 준비한다.
            Slice<Message> sliceMessage = createMessageSlice(messages, pageable, false);

            // 첫 번째 메시지에만 첨부 파일이 있는 상황을 만든다.
            // MessageFile은 getMessageId() 기준으로 그룹핑되므로, 첫 번째 메시지 ID를 반환하도록 stub 한다.
            // 두 번째 메시지 ID를 반환하는 MessageFile은 일부러 만들지 않는다.
            // 그래야 두 번째 메시지가 Map 조회에서 누락되고 getOrDefault(..., List.of()) 경로를 타게 된다.
            MessageFile firstMessageFile = mock(MessageFile.class);
            given(firstMessageFile.getMessageId()).willReturn(firstMessageId);
            List<MessageFile> messageFiles = List.of(firstMessageFile);

            // 서비스 내부에서는 messageId별로 그룹핑된 첨부 파일 목록을 mapper에 전달한다.
            // 첫 번째 메시지는 첨부 파일 1개, 두 번째 메시지는 첨부 파일이 없으므로 빈 리스트가 전달되어야 한다.
            // 아래 리스트들은 stubbing용 기대값이며, 실제 전달 인자는 ArgumentCaptor로 다시 확인한다.
            List<MessageFile> firstMessageFiles = List.of(firstMessageFile);
            List<MessageFile> secondMessageFiles = List.of();

            // DTO 변환 결과는 실제 record 객체로 준비한다.
            // 이 테스트의 관심사는 DTO 내부 필드 계산이 아니라,
            // 서비스가 MessageMapper 변환 결과를 DTO Slice로 유지하고 PageResponseMapper 응답을 그대로 반환하는지이다.
            MessageDto firstMessageDto = createMessageDto(
                    firstMessageId,
                    OffsetDateTime.now().minusSeconds(10),
                    "firstMessage",
                    channelId,
                    UUID.randomUUID(),
                    List.of()
            );
            MessageDto secondMessageDto = createMessageDto(
                    secondMessageId,
                    OffsetDateTime.now(),
                    "secondMessage",
                    channelId,
                    UUID.randomUUID(),
                    List.of()
            );
            List<MessageDto> messageDtos = List.of(firstMessageDto, secondMessageDto);

            // 다음 페이지가 없으면 nextCursor는 null이어야 한다.
            // PageResponse 생성은 PageResponseMapper 책임이므로, 서비스는 이 응답을 그대로 반환하는지만 확인한다.
            PageResponse<MessageDto> pageResponse = createPageResponse(messageDtos, null, pageable, false);

            // repository가 다음 페이지가 없는 메시지 Slice를 반환하는 상황을 만든다.
            given(messageRepository.findAllByCondition(condition))
                    .willReturn(sliceMessage);

            // 서비스는 Slice에 담긴 메시지 ID 목록으로 첨부 파일을 한 번에 조회한다.
            given(messageFileService.findAllByMessageIds(messageIds))
                    .willReturn(messageFiles);

            // 첫 번째 메시지는 연결된 첨부 파일 목록과 함께 DTO로 변환되어야 한다.
            given(messageMapper.toDto(firstMessage, firstMessageFiles))
                    .willReturn(firstMessageDto);

            // 두 번째 메시지는 첨부 파일이 없으므로 빈 리스트와 함께 DTO로 변환되어야 한다.
            given(messageMapper.toDto(secondMessage, secondMessageFiles))
                    .willReturn(secondMessageDto);

            // 서비스 내부에서 Slice.map(...)으로 새로운 Slice<MessageDto>가 만들어진다.
            // 따라서 특정 Slice 인스턴스로 stubbing하지 않고 any()로 받고, null cursor는 isNull()로 의도를 드러낸다.
            given(messageDtoPageResponseMapper.fromSlice(any(), isNull()))
                    .willReturn(pageResponse);

            // when
            // 채널별 메시지 목록을 조회한다.
            PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, pageable, cursor);

            // then
            // 서비스는 PageResponseMapper가 만든 응답을 그대로 반환해야 한다.
            assertThat(result).isEqualTo(pageResponse);
            assertThat(result.content()).containsExactlyElementsOf(messageDtos);
            assertThat(result.nextCursor()).isNull();
            assertThat(result.hasNext()).isFalse();

            InOrder inOrder = inOrder(messageRepository, messageFileService, messageMapper, messageDtoPageResponseMapper);

            // 1. 채널 ID, 페이지 조건, cursor를 담은 condition으로 메시지 Slice를 조회한다.
            inOrder.verify(messageRepository).findAllByCondition(condition);

            // 2. 조회된 메시지 ID 목록으로 첨부 파일을 한 번에 조회한다.
            inOrder.verify(messageFileService).findAllByMessageIds(messageIds);

            // 3. 각 메시지는 자신에게 매칭된 첨부 파일 목록과 함께 DTO로 변환된다.
            // 여기서는 messageMapper.toDto(...)의 두 번째 인자를 캡처해서,
            // 첨부 파일이 없는 메시지에 null이 아니라 빈 리스트가 전달되는지 직접 검증한다.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<MessageFile>> messageFilesCaptor = ArgumentCaptor.forClass(List.class);
            inOrder.verify(messageMapper).toDto(eq(firstMessage), messageFilesCaptor.capture());
            inOrder.verify(messageMapper).toDto(eq(secondMessage), messageFilesCaptor.capture());

            List<List<MessageFile>> capturedMessageFilesByCall = messageFilesCaptor.getAllValues();
            assertThat(capturedMessageFilesByCall).hasSize(2);
            assertThat(capturedMessageFilesByCall.get(0)).containsExactly(firstMessageFile);
            assertThat(capturedMessageFilesByCall.get(1)).isEmpty();

            // 4. 다음 페이지가 없으므로 PageResponseMapper에는 null nextCursor가 전달되어야 한다.
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Slice<MessageDto>> sliceCaptor = ArgumentCaptor.forClass(Slice.class);
            inOrder.verify(messageDtoPageResponseMapper).fromSlice(sliceCaptor.capture(), isNull());

            // PageResponseMapper에 전달된 DTO Slice의 내용과 페이지 상태를 확인한다.
            Slice<MessageDto> capturedDtoSlice = sliceCaptor.getValue();
            assertThat(capturedDtoSlice.getContent()).containsExactlyElementsOf(messageDtos);
            assertThat(capturedDtoSlice.hasNext()).isFalse();
            assertThat(capturedDtoSlice.getSize()).isEqualTo(pageable.getPageSize());

            // hasNext=false이면 DTO에 createdAt 값이 있더라도 nextCursor는 null이어야 한다.
            // 이 값은 위 fromSlice(...) 검증에서 isNull()로 이미 확인한다.

            // 위에서 검증한 목록 조회 성공 흐름 외에 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(messageRepository, messageFileService, messageMapper, messageDtoPageResponseMapper);

            // 채널별 메시지 목록 조회는 사용자/채널 엔티티를 별도로 조회하지 않는다.
            // channelId는 repository 조회 조건으로만 사용되므로 UserReader, ChannelReader는 호출되면 안 된다.
            verifyNoInteractions(userReader, channelReader);
        }

        @Test
        @DisplayName("채널별 메시지 목록 조회 실패 - channelId가 null이면 조회 조건 생성 실패")
        void findAllByChannelId_throwsMessageInvalidPagingConditionException_whenChannelIdIsNull() {
            // given
            // BasicMessageService는 Repository에 넘기기 전에 MessagePagingCondition을 만든다.
            // channelId는 메시지 목록 조회의 필수 조건이므로 null이면 Repository 호출 전에 실패해야 한다.
            UUID channelId = null;
            Pageable pageable = PageRequest.of(0, 10);
            UUID cursor = UUID.randomUUID();

            // when / then
            assertThatThrownBy(() -> messageService.findAllByChannelId(channelId, pageable, cursor))
                    .isInstanceOfSatisfying(MessageInvalidPagingConditionException.class, exception -> {
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_INVALID_PAGING_CONDITION);
                        assertThat(exception.getDetails()).containsEntry("parameter", "channelId");
                    });

            verifyNoInteractions(
                    messageRepository,
                    messageFileService,
                    messageMapper,
                    messageDtoPageResponseMapper,
                    userReader,
                    channelReader
            );
        }
    }

    @Nested
    @DisplayName("메시지 수정")
    class UpdateMessageTest {

        @Test
        @DisplayName("메시지 수정 성공")
        void update_returnsMessageDto_whenMessageExists() {
            // given
            // 수정 대상 메시지 ID와 수정 요청 command를 준비한다.
            UUID messageId = UUID.randomUUID();
            MessageUpdateCommand command = new MessageUpdateCommand("newMessage");

            // Message.updateInfo(...)의 상태 변경이 이 테스트의 핵심 관심사이므로 실제 Message 엔티티를 사용한다.
            // User와 Channel은 Message 생성에 필요한 단순 참조 역할만 하므로 mock으로 충분하다.
            MessageCreateCommand originalCommand = createMessageCommand();
            User author = mock(User.class);
            Channel channel = mock(Channel.class);
            Message message = new Message(author, channel, originalCommand);
            ReflectionTestUtils.setField(message, "id", messageId);

            // 수정 후 DTO 변환에 필요한 기존 첨부 파일 목록을 준비한다.
            // 메시지 수정은 첨부 파일을 추가/삭제하지 않으므로 현재 첨부 파일 목록을 다시 조회해서 mapper에 넘겨야 한다.
            List<MessageFile> messageFiles = List.of();

            // mapper가 반환할 최종 DTO는 실제 record 객체로 만든다.
            // 실제 Message -> MessageDto 필드 매핑은 MessageMapper 테스트에서 별도로 검증한다.
            MessageDto messageDto = createMessageDto(
                    messageId,
                    null,
                    command.content(),
                    originalCommand.channelId(),
                    originalCommand.userId(),
                    List.of()
            );

            // 수정 대상 메시지가 존재하는 상황을 만든다.
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

            // service.update(...)는 updateInfo(...) 호출 후 repository.save(message)의 반환값을 DTO 변환에 사용한다.
            // repository는 mock이므로 실제 DB 저장은 검증하지 않고, 저장 요청 객체를 그대로 반환하게 한다.
            given(messageRepository.save(message)).willReturn(message);

            // 저장된 메시지의 첨부 파일 목록을 조회한 뒤 DTO 변환에 사용하도록 준비한다.
            given(messageFileService.findAllByMessageId(messageId)).willReturn(messageFiles);
            given(messageMapper.toDto(message, messageFiles)).willReturn(messageDto);

            // when
            // 메시지 수정 로직을 실행한다.
            MessageDto result = messageService.update(messageId, command);

            // then
            // 실제 Message 엔티티를 사용했으므로 updateInfo(...) 호출 여부보다 상태 변경 결과를 검증한다.
            assertThat(message.getContent()).isEqualTo(command.content());

            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(messageDto);

            // 수정 성공 흐름의 핵심 협력 객체 호출을 확인한다.
            verify(messageRepository).findById(messageId);
            verify(messageRepository).save(message);
            verify(messageFileService).findAllByMessageId(messageId);
            verify(messageMapper).toDto(message, messageFiles);

            // 위에서 검증한 수정 성공 흐름 외 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(messageRepository, messageFileService, messageMapper);

            // 메시지 수정은 작성자/채널 엔티티를 새로 조회하지 않고,
            // 페이지 응답도 만들지 않으므로 아래 협력 객체들이 호출되면 안 된다.
            verifyNoInteractions(userReader, channelReader, messageDtoPageResponseMapper);
        }
        @Test
        @DisplayName("메시지 수정 실패 - 존재하지 않는 메시지")
        void update_throwsMessageNotFoundException_whenMessageDoesNotExist() {
            // given
            // 수정 대상 메시지 ID와 수정 요청 command를 준비한다.
            // 이 테스트에서는 메시지가 존재하지 않으므로 command의 content 값은 실제로 사용되면 안 된다.
            UUID messageId = UUID.randomUUID();
            MessageUpdateCommand command = new MessageUpdateCommand("newMessage");

            // repository가 메시지를 찾지 못하는 상황을 만든다.
            // BasicMessageService.update(...)는 내부에서 getMessageRequireThrow(...)를 호출하고,
            // findById(...) 결과가 비어 있으면 즉시 MessageNotFoundException을 던져야 한다.
            given(messageRepository.findById(messageId)).willReturn(Optional.empty());

            // when & then
            // 존재하지 않는 메시지를 수정하려고 하면 MessageNotFoundException이 발생해야 한다.
            // 예외 details에도 조회에 실패한 messageId가 담겨야 이후 예외 응답에서 원인을 추적할 수 있다.
            assertThatThrownBy(() -> messageService.update(messageId, command))
                    .isInstanceOf(MessageNotFoundException.class)
                    .satisfies(exception -> {
                        MessageNotFoundException messageNotFoundException = (MessageNotFoundException) exception;
                        assertThat(messageNotFoundException.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
                        assertThat(messageNotFoundException.getDetails()).containsEntry("messageId", messageId);
                    });

            // 수정 실패 흐름에서는 메시지 존재 여부 조회까지만 실행되어야 한다.
            verify(messageRepository).findById(messageId);
            verifyNoMoreInteractions(messageRepository);

            // 메시지를 찾지 못했으므로 updateInfo(...), save(...), 첨부 파일 조회, DTO 변환으로 진행하면 안 된다.
            verifyNoInteractions(messageFileService, messageMapper);

            // 메시지 수정은 작성자/채널 엔티티를 새로 조회하지 않고,
            // 페이지 응답도 만들지 않으므로 아래 협력 객체들이 호출되면 안 된다.
            verifyNoInteractions(userReader, channelReader, messageDtoPageResponseMapper);
        }
    }

    @Nested
    @DisplayName("메시지 삭제")
    class DeleteMessageTest {

        @Test
        @DisplayName("메시지 삭제 성공")
        void delete_deletesMessageAndRelatedFiles_whenMessageExists() {
            // given
            // 삭제 대상 메시지 ID를 준비한다.
            // BasicMessageService.delete(...)는 먼저 해당 ID의 메시지가 존재하는지 확인한다.
            UUID messageId = UUID.randomUUID();

            // 메시지가 존재하는 상황을 만든다.
            // existsById(...)가 true이면 서비스는 첨부 파일 연결 삭제와 메시지 삭제를 계속 진행해야 한다.
            given(messageRepository.existsById(messageId)).willReturn(true);

            // when
            // 메시지 삭제 로직을 실행한다.
            messageService.delete(messageId);

            // then
            InOrder inOrder = inOrder(messageRepository, messageFileService);

            // 1. 삭제 전에 메시지가 실제로 존재하는지 확인한다.
            inOrder.verify(messageRepository).existsById(messageId);

            // 2. 메시지를 삭제하기 전에 연결된 첨부 파일 정보를 먼저 삭제한다.
            // 이 순서가 바뀌면 메시지 파일이 메시지를 참조하는 구조에서 FK/고아 데이터 문제가 생길 수 있다.
            inOrder.verify(messageFileService).deleteByMessageId(messageId);

            // 3. 첨부 파일 연결 정리가 끝난 뒤 메시지를 삭제한다.
            inOrder.verify(messageRepository).deleteById(messageId);

            // 위에서 검증한 삭제 성공 흐름 외 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(messageRepository, messageFileService);

            // 메시지 삭제는 사용자/채널 엔티티를 조회하지 않고 DTO도 반환하지 않는다.
            // 따라서 reader, mapper, page mapper가 호출되면 삭제 책임 범위를 벗어난 동작이다.
            verifyNoInteractions(userReader, channelReader, messageMapper, messageDtoPageResponseMapper);
        }

        @Test
        @DisplayName("메시지 삭제 실패 - 존재하지 않는 메시지")
        void delete_throwsMessageNotFoundException_whenMessageDoesNotExist() {
            // given
            // 삭제 대상 메시지 ID를 준비한다.
            // 이 ID에 해당하는 메시지가 repository에 없다는 상황을 검증할 것이다.
            UUID messageId = UUID.randomUUID();

            // BasicMessageService.delete(...)는 findById(...)로 엔티티를 가져오지 않고
            // existsById(...)로 존재 여부만 확인한 뒤 삭제를 진행한다.
            // 여기서는 존재하지 않는 메시지이므로 false를 반환하도록 설정한다.
            given(messageRepository.existsById(messageId)).willReturn(false);

            // when & then
            // 존재하지 않는 메시지를 삭제하려고 하면 MessageNotFoundException이 발생해야 한다.
            // 예외에는 MESSAGE_NOT_FOUND 코드와 실패한 messageId가 함께 담겨야 한다.
            assertThatThrownBy(() -> messageService.delete(messageId))
                    .isInstanceOf(MessageNotFoundException.class)
                    .satisfies(exception -> {
                        MessageNotFoundException messageNotFoundException = (MessageNotFoundException) exception;
                        assertThat(messageNotFoundException.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
                        assertThat(messageNotFoundException.getDetails()).containsEntry("messageId", messageId);
                    });

            // 삭제 실패 흐름에서는 메시지 존재 여부 확인까지만 실행되어야 한다.
            verify(messageRepository).existsById(messageId);
            verify(messageRepository, never()).deleteById(messageId);

            // existsById(...) 외에 repository 호출이 추가로 발생하면 안 된다.
            verifyNoMoreInteractions(messageRepository);

            // 메시지가 없으므로 첨부 파일 삭제도 진행되면 안 된다.
            verifyNoInteractions(messageFileService);

            // 메시지 삭제 실패 흐름은 사용자/채널 조회나 DTO 변환, 페이지 응답 생성과 무관하다.
            verifyNoInteractions(userReader, channelReader, messageMapper, messageDtoPageResponseMapper);
        }
    }

    @Nested
    @DisplayName("채널별 메시지 전체 삭제")
    class DeleteAllByChannelTest {

        @Test
        @DisplayName("채널별 메시지 전체 삭제 성공 - 삭제할 메시지가 있음")
        void deleteAllByChannelId_deletesMessagesAndRelatedFiles_whenMessagesExist() {
            // given
            // 삭제 대상 채널 ID를 준비한다.
            // BasicMessageService.deleteAllByChannelId(...)는 channelId로 삭제 대상 메시지가 있는지 먼저 확인한다.
            UUID channelId = UUID.randomUUID();

            // 해당 채널에 삭제할 메시지가 존재하는 상황을 만든다.
            // existsByChannel_Id(...)가 true이면 서비스는 첨부 파일 정리와 메시지 삭제를 계속 진행해야 한다.
            given(messageRepository.existsByChannel_Id(channelId)).willReturn(true);

            // when
            // 채널에 속한 모든 메시지를 삭제한다.
            messageService.deleteAllByChannelId(channelId);

            // then
            // 채널별 전체 삭제는 연관 첨부 파일을 먼저 정리한 뒤 메시지를 삭제해야 하므로 순서가 의미 있다.
            InOrder inOrder = inOrder(messageRepository, messageFileService);

            // 1. channelId에 해당하는 메시지가 존재하는지 확인한다.
            inOrder.verify(messageRepository).existsByChannel_Id(channelId);

            // 2. 메시지 본문을 삭제하기 전에 해당 채널 메시지들의 첨부 파일 연결과 실제 파일 삭제를 먼저 위임한다.
            // 이 순서가 바뀌면 메시지 삭제 후 첨부 파일 조회/삭제 기준을 잃거나 연관 데이터가 남을 수 있다.
            inOrder.verify(messageFileService).deleteAllByChannelId(channelId);

            // 3. 첨부 파일 정리가 끝난 뒤 channelId에 속한 메시지를 모두 삭제한다.
            inOrder.verify(messageRepository).deleteAllByChannel_Id(channelId);

            // 위에서 검증한 전체 삭제 흐름 외 추가 호출이 없는지 확인한다.
            verifyNoMoreInteractions(messageRepository, messageFileService);

            // 채널별 메시지 전체 삭제는 사용자/채널 엔티티를 별도로 조회하지 않고 DTO도 만들지 않는다.
            // 따라서 reader, mapper, page mapper가 호출되면 서비스 책임 범위를 벗어난 동작이다.
            verifyNoInteractions(userReader, channelReader, messageMapper, messageDtoPageResponseMapper);
        }

        @Test
        @DisplayName("채널별 메시지 전체 삭제 성공 - 삭제할 메시지가 없음")
        void deleteAllByChannelId_doesNothing_whenMessagesDoNotExist() {
            // given
            // 삭제 대상 채널 ID를 준비한다.
            UUID channelId = UUID.randomUUID();

            // 해당 채널에 삭제할 메시지가 없는 상황을 만든다.
            // BasicMessageService.deleteAllByChannelId(...)는 이 경우 조용히 종료해야 한다.
            given(messageRepository.existsByChannel_Id(channelId)).willReturn(false);

            // when
            // 채널별 메시지 전체 삭제를 요청한다.
            messageService.deleteAllByChannelId(channelId);

            // then
            // 삭제할 메시지가 있는지 확인하는 조회까지만 실행되어야 한다.
            verify(messageRepository).existsByChannel_Id(channelId);

            // 메시지가 없으면 repository 삭제 쿼리를 호출하면 안 된다.
            verify(messageRepository, never()).deleteAllByChannel_Id(channelId);
            verifyNoMoreInteractions(messageRepository);

            // 메시지가 없으면 첨부 파일 삭제도 진행할 대상이 없으므로 호출되면 안 된다.
            verifyNoInteractions(messageFileService);

            // 메시지 전체 삭제의 no-op 흐름은 사용자/채널 조회나 DTO 변환, 페이지 응답 생성과 무관하다.
            verifyNoInteractions(userReader, channelReader, messageMapper, messageDtoPageResponseMapper);
        }
    }

    @Nested
    @DisplayName("작성자별 메시지 작성자 연결 해제")
    class DetachAuthorTest {

        @Test
        @DisplayName("작성자별 메시지 작성자 연결 해제 성공 - 작성자의 메시지가 있음")
        void detachByAuthorId_detachesAuthor_whenAuthorMessagesExist() {
            // given
            // 연결 해제 대상 작성자 ID를 준비한다.
            // detachByAuthorId(...)는 사용자 엔티티를 조회하지 않고 authorId 조건으로 메시지의 작성자 참조를 끊는다.
            UUID authorId = UUID.randomUUID();

            // 해당 작성자가 작성한 메시지가 존재하는 상황을 만든다.
            // existsByAuthor_Id(...)가 true이면 서비스는 bulk update 메서드인 detachAuthorByAuthorId(...)를 호출해야 한다.
            given(messageRepository.existsByAuthor_Id(authorId)).willReturn(true);

            // when
            // 작성자 삭제 또는 탈퇴 흐름에서 메시지의 작성자 연결을 해제한다.
            messageService.detachByAuthorId(authorId);

            // then
            // 먼저 해당 작성자의 메시지가 있는지 확인한다.
            verify(messageRepository).existsByAuthor_Id(authorId);

            // 작성자 메시지가 존재하면 메시지를 삭제하지 않고 author 참조만 null로 변경하는 repository bulk update를 호출한다.
            // 실제 JPQL update가 올바르게 동작하는지는 Repository 또는 통합 테스트에서 검증한다.
            verify(messageRepository).detachAuthorByAuthorId(authorId);

            // 이 서비스 메서드의 repository 책임은 존재 여부 확인과 작성자 참조 해제까지이다.
            verifyNoMoreInteractions(messageRepository);

            // 작성자 연결 해제는 첨부 파일, DTO 변환, 페이지 응답 생성, 사용자/채널 조회와 무관하다.
            verifyNoInteractions(userReader, channelReader, messageMapper, messageFileService, messageDtoPageResponseMapper);
        }

        @Test
        @DisplayName("작성자별 메시지 작성자 연결 해제 성공 - 작성자의 메시지가 없음")
        void detachByAuthorId_doesNothing_whenAuthorMessagesDoNotExist() {
            // given
            // 연결 해제 대상 작성자 ID를 준비한다.
            UUID authorId = UUID.randomUUID();

            // 해당 작성자가 작성한 메시지가 없는 상황을 만든다.
            // BasicMessageService.detachByAuthorId(...)는 이 경우 bulk update 없이 조용히 종료해야 한다.
            given(messageRepository.existsByAuthor_Id(authorId)).willReturn(false);

            // when
            // 작성자 연결 해제를 요청한다.
            messageService.detachByAuthorId(authorId);

            // then
            // 작성자의 메시지가 있는지 확인하는 조회까지만 실행되어야 한다.
            verify(messageRepository).existsByAuthor_Id(authorId);

            // 작성자의 메시지가 없으면 업데이트할 row가 없으므로 bulk update를 호출하지 않는다.
            verify(messageRepository, never()).detachAuthorByAuthorId(authorId);
            verifyNoMoreInteractions(messageRepository);

            // no-op 흐름에서는 첨부 파일 처리, DTO 변환, 페이지 응답 생성, 사용자/채널 조회가 필요 없다.
            verifyNoInteractions(userReader, channelReader, messageMapper, messageFileService, messageDtoPageResponseMapper);
        }
    }

    private MockMultipartFile createMockMultipartFile() {
        return new MockMultipartFile("fileName", "fileOriginFileName", "fileContent", "file".getBytes());
    }

    private MessageCreateCommand createMessageCommand() {
        return new MessageCreateCommand("messageContent", UUID.randomUUID(), UUID.randomUUID());
    }

    private MessageDto createMessageDto(UUID messageId, MessageCreateCommand command, List<BinaryContentDto> binaryContentDtos) {
        return createMessageDto(
                messageId,
                null,
                command.content(),
                command.channelId(),
                command.userId(),
                binaryContentDtos
        );
    }

    private MessageDto createMessageDto(UUID messageId, OffsetDateTime createdAt, String content, UUID channelId, UUID userId, List<BinaryContentDto> binaryContentDtos) {
        return new MessageDto(
                messageId,
                createdAt,
                null,
                content,
                channelId,
                new UserDto(userId, null, null, null, false, Role.USER, null, null),
                binaryContentDtos
        );
    }
    private PageResponse<MessageDto> createPageResponse(MessageDto messageDto, Object nextCursor, Pageable pageable, boolean hasNext) {
        return new PageResponse<>(
                List.of(messageDto),
                nextCursor,
                pageable.getPageSize(),
                hasNext
        );
    }

    private PageResponse<MessageDto> createPageResponse(List<MessageDto> messageDtos, Object nextCursor, Pageable pageable, boolean hasNext) {
        return new PageResponse<>(
                messageDtos,
                nextCursor,
                pageable.getPageSize(),
                hasNext
        );
    }

    private SliceImpl<Message> createMessageSlice(List<Message> messages, Pageable pageable, boolean hasNext) {
        return new SliceImpl<>(messages, pageable, hasNext);
    }

}
