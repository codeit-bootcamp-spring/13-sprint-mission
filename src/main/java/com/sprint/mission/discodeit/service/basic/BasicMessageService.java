package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.FileUploadDto;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final MessageMapper messageMapper;

  @Override
  @Transactional
  public MessageDto create(UUID channelId, UUID authorId, String content,
      List<FileUploadDto> files) {
    log.debug("Message 생성 요청 - channelId: {}, authorId: {}", channelId, authorId);

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("Message 생성 실패 - 존재하지 않는 channelId: {}", channelId);
          return new ChannelNotFoundException(Map.of("channelId", channelId));
        });
    User author = userRepository.findById(authorId)
        .orElseThrow(() -> {
          log.warn("Message 생성 실패 - 존재하지 않는 authorId: {}", authorId);
          return new UserNotFoundException(Map.of("authorId", authorId));
        });

    Message message = new Message(channel, author, content);

    if (files != null) {
      for (FileUploadDto file : files) {
        if (file.bytes() == null || file.bytes().length == 0) {
          continue;
        }

        String fileName = file.fileName();
        long fileSize = file.size();
        String contentType = file.contentType();
        String fileUrl = "/api/binaryContents/";

        BinaryContent attachment = new BinaryContent(fileName, fileUrl, fileSize, contentType);
        message.addAttachment(attachment);
      }
    }

    messageRepository.save(message);

    if (files != null) {
      List<BinaryContent> savedAttachments = message.getAttachments();
      int index = 0;

      for (FileUploadDto file : files) {
        if (file.bytes() == null || file.bytes().length == 0) {
          continue;
        }

        eventPublisher.publishEvent(
            new BinaryContentCreatedEvent(
                savedAttachments.get(index).getId(),
                file.bytes()
            )
        );

        index++;
      }
    }

    log.info("Message 생성 완료 - messageId: {}", message.getId());

    MessageDto messageDto = messageMapper.toDto(message);

    eventPublisher.publishEvent(
        new MessageCreatedEvent(
            channel.getId(),
            author.getId(),
            author.getUsername(),
            channel.getName() != null ? channel.getName() : "개인 채널",
            message.getContent(),
            messageDto
        )
    );

    return messageDto;
  }

  @Override
  public MessageDto read(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new MessageNotFoundException(Map.of("messageId", id)));

    return messageMapper.toDto(message);
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable) {
    Slice<Message> slice;

    if (cursor == null) {
      slice = messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable);
    } else {
      slice = messageRepository.findByChannelIdAndCreatedAtLessThanOrderByCreatedAtDesc(channelId,
          cursor, pageable);
    }

    List<MessageDto> content = slice.getContent().stream()
        .map(messageMapper::toDto)
        .toList();

    Instant nextCursor = null;
    if (slice.hasNext() && !slice.getContent().isEmpty()) {
      nextCursor = slice.getContent().get(slice.getNumberOfElements() - 1).getCreatedAt();
    }

    return new PageResponse<>(content, nextCursor, slice.getSize(), slice.hasNext(), null);
  }

  @Override
  @Transactional
  public MessageDto update(UUID id, String newContent) {
    log.debug("Message 수정 요청 - messageId: {}", id);
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Message 수정 실패 - 존재하지 않는 messageId: {}", id);
          return new MessageNotFoundException(Map.of("messageId", id));
        });

    message.update(newContent);
    log.info("Message 수정 완료 - messageId: {}", id);
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("Message 삭제 요청 - messageId: {}", id);
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Message 삭제 실패 - 존재하지 않는 messageId: {}", id);
          return new MessageNotFoundException(Map.of("messageId", id));
        });

    messageRepository.delete(message);
    log.info("Message 삭제 완료 - messageId: {}", id);
  }
}