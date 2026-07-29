package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.InvalidMessageContentException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentService binaryContentService;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest request) {
        log.info("Creating message: channelId={}, userId={}", request.channelId(), request.userId());
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

        User author = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        if ((request.content() == null || request.content().isBlank())
                && (request.attachments() == null || request.attachments().isEmpty())) {
            log.warn("Rejected empty message creation: channelId={}, userId={}", request.channelId(), request.userId());
            throw new InvalidMessageContentException(request.channelId(), request.userId());
        }

        List<BinaryContent> attachments = new ArrayList<>();

        if (request.attachments() != null) {
            for (BinaryContentCreateRequest attachmentRequest : request.attachments()) {
                BinaryContent attachment = binaryContentService.createEntity(attachmentRequest);
                attachments.add(attachment);
            }
        }

        Message message = new Message(
                request.content(),
                author,
                channel,
                attachments
        );

        messageRepository.save(message);
        return messageMapper.toDto(message);
    }



    @Override
    public MessageDto findById(UUID id) {
        Message message = messageRepository.findWithDetailsById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        return messageMapper.toDto(message);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, Pageable pageable) {
        Slice<Message> messages = cursor == null
                ? messageRepository.findAllByChannel_Id(channelId, pageable)
                : messageRepository.findAllByChannel_IdAndCreatedAtLessThan(channelId, cursor, pageable);

        Slice<MessageDto> messageDtos = messages.map(messageMapper::toDto);

        return pageResponseMapper.fromSlice(messageDtos, MessageDto::createdAt);
    }


    @Override
    @Transactional
    public MessageDto update(UUID messageId, MessageUpdateRequest request) {
        log.info("Updating message: messageId={}", messageId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        message.update(request.content());
        messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting message: messageId={}", id);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));

        List<UUID> attachmentIds = message.getAttachments().stream()
                .map(BinaryContent::getId)
                .toList();

        messageRepository.delete(message);

        for (UUID attachmentId : attachmentIds) {
            binaryContentService.delete(attachmentId);
        }
    }
    }
