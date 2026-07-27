package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.binarycontent.*;
import com.sprint.mission.discodeit.exception.channel.*;
import com.sprint.mission.discodeit.exception.message.*;
import com.sprint.mission.discodeit.exception.user.*;
import com.sprint.mission.discodeit.mapper.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.core.io.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentService binaryContentService;

    @Override
    public MessageDto find(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public MessageDto create(CreateMessageCommand command,
                             List<CreateBinaryContentCommand> attachmentCommands) {

        if (command == null) {
            throw new IllegalArgumentException("메시지 생성 요청은 필수입니다.");
        }

        if (command.channelId() == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (command.authorId() == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다.");
        }

        log.info(
                "메시지 생성 요청. channelId={}, authorId={}",
                command.channelId(),
                command.authorId()
        );

        Channel channel = channelRepository.findById(command.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(command.channelId()));

        User author = userRepository.findById(command.authorId())
                .orElseThrow(() -> new UserNotFoundException(command.authorId()));

        List<CreateBinaryContentCommand> commands =
                attachmentCommands == null
                        ? List.of()
                        : attachmentCommands;
        List<BinaryContent> attachments = commands.stream()
                .map(binaryContentService::create)
                .map(BinaryContentDto::id)
                .map(attachmentId ->
                        binaryContentRepository.findById(attachmentId)
                                .orElseThrow(() ->
                                        new BinaryContentNotFoundException(attachmentId)
                                )
                )
                .toList();

        Message message = new Message(
                command.content(),
                channel,
                author,
                attachments
        );

        Message savedMessage =
                messageRepository.save(message);

        log.info("메세지 생성 완료. id = {}", savedMessage.getId());
        return messageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional
    public MessageDto update(UUID id, UpdateMessageCommand command) {
        if (id == null) {
            throw new IllegalArgumentException("메시지 ID는 필수입니다.");
        }

        if (command == null) {
            throw new IllegalArgumentException("메시지 수정 요청은 필수입니다.");
        }

        log.info("메시지 수정 요청. id={}", id);

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        message.update(command.content());

        log.info("메시지 수정 완료. id={}", id);
        return messageMapper.toDto(message);
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("메세지 ID는 필수입니다.");
        }
        log.info("메시지 삭제 요청. id={}", id);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        messageRepository.delete(message);
        log.info("메시지 삭제 완료. id={}", id);
    }

    @Override
    public PageResponse<MessageDto> getMessages(UUID channelId, Instant cursor, Pageable pageable) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다."); }

        if (pageable == null) {
            throw new IllegalArgumentException("페이징 정보는 필수입니다.");
        }
        Slice<Message> messageSlice;

        if (cursor == null) {
            messageSlice = messageRepository.findByChannelId(
                    channelId,
                    pageable
            );
        } else {
            messageSlice = messageRepository
                    .findByChannelIdAndCreatedAtLessThan(
                            channelId,
                            cursor,
                            pageable
                    );
        }

        Slice<MessageDto> responseSlice =
                messageSlice.map(messageMapper::toDto);

        Instant nextCursor = null;

        if (responseSlice.hasNext() && !responseSlice.isEmpty()) {
            List<MessageDto> content = responseSlice.getContent();

            nextCursor = content
                    .get(content.size() - 1)
                    .createdAt();
        }

        return PageResponseMapper.fromSlice(
                responseSlice,
                nextCursor
        );

        }
    }

