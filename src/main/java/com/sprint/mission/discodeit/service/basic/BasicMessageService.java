package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MultipartFileDto;
import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicMessageService implements MessageService {

    private final String SERVICE_NAME = "MessageService";

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final PageResponseMapper pageResponseMapper;
    private final EntityMapper entityMapper;

    private final ApplicationEventPublisher eventPublisher;

    private User getUserOrException(UUID id){
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(
                        "{} - User with id - {} not found",
                        SERVICE_NAME,
                        id
                )
        );
    }

    private Channel getChannelOrException(UUID id){
        return channelRepository.findById(id).orElseThrow(
                () -> new ChannelNotFoundException(
                        "{} - Channel with id - {} not found",
                        SERVICE_NAME,
                        id
                )
        );
    }


    @Override
    @Transactional
    public MessageDto createMessage(MessageCreateRequest request, List<MultipartFileDto> attachments){
        User user = getUserOrException(request.authorId());
        Channel channel = getChannelOrException(request.channelId());

        List<BinaryContent> attachFiles = attachments.stream().map(this::getBinaryContent).toList();

        Message message = new Message(
                request.content(),
                channel,
                user,
                attachFiles
        );

        messageRepository.save(message);

        eventPublisher.publishEvent(new MessageCreateEvent(
                message.getId(),
                channel.getId(),
                user.getId(),
                request.content()
        ));

        log.debug("Message Created - {}", message.getId());

        return entityMapper.toDto(message);
    }

    private BinaryContent getBinaryContent(MultipartFileDto fileDto){
        BinaryContent binarycontent = binaryContentRepository.save(fileDto.toBinaryContent());
        eventPublisher.publishEvent(new BinaryContentCreatedEvent(
                binarycontent.getId(),
                fileDto.content()
        ));
        return binarycontent;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageDto> findallByChannelIdWithCursor(
            UUID channelId,
            Pageable pageable,
            Instant cursor
    ){
        Instant cursorTime = Optional.of(cursor).orElse(Instant.now());

        // 1. query target messages id
        Slice<UUID> query =
                messageRepository.findMessageIdsByChannelIdWithCursor(
                        channelId,
                        pageable,
                        cursorTime
                );


        // 2. query required info about message.
        Map<UUID, Message> messageList = messageRepository.findAllById(query.getContent()).stream()
                .collect(
                        Collectors.toMap(
                                Message::getId,
                                message -> message
                        )
                );

        // 3. construct MessageDto
        List<MessageDto> contents = query.stream()
                .map(messageId -> {
                    MessageDto dto = entityMapper.toDto(messageList.get(messageId));

                    if (dto == null) {
                        log.error(
                                "[{}] - DTO conversion error. no message entity with - id : {}",
                                SERVICE_NAME,
                                messageId
                        );
                        throw new DiscodeitException(ExceptionCode.SERVER_ERROR, "메세지 -> DTO 변환 에러!");
                    }
                    return dto;
                })
                .toList();

        // 4. create Slice Object from query
        Slice<MessageDto> slice = new SliceImpl<>(contents,Pageable.ofSize(query.getSize()),query.hasNext());

        Instant newCursor = getLastMessageTime(contents, cursorTime);

        return pageResponseMapper.fromSliceWithCursor(slice,newCursor);
    }

    // NPE 방지 분기.
    private Instant getLastMessageTime(List<MessageDto> messageList,Instant cursor){
        Instant originCursor = Optional.of(cursor).orElse(null);
        try{
            if(messageList.isEmpty()) return originCursor;
            else return messageList.get(messageList.size() - 1).createdAt();
        } catch (NullPointerException e) {
            log.error("[{}] - 메세지 변환 에러! - Cursor 변환시 null 발견됨.", SERVICE_NAME, e);
            throw new DiscodeitException(ExceptionCode.SERVER_ERROR, "메세지 -> cursor 변환 에러!");
        }
    }

    @Override
    @Transactional
    public MessageDto updateMessageData(UUID id, MessageUpdateRequest request){
        Message message = getMessageOrException(id);

        message.update(request.newContent());

        log.info("Message Updated - {}", message.getId());

        return entityMapper.toDto(message);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID id){
        Message message = getMessageOrException(id);

        // 실제 데이터 삭제로직 추가.
        binaryContentRepository.deleteAll(message.getAttachment());
        messageRepository.delete(message);

        log.info("Message Deleted - {}", message.getId());
    }




    private Message getMessageOrException(UUID id){
        return messageRepository.findById(id)
                .orElseThrow(
                        () -> new MessageNotFoundException(
                                "Message with id - {} not found",
                                SERVICE_NAME,
                                id
                        )
                );
    }

}
