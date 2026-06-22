package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Primary
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public MessageResponse create(MessageCreateRequest request, List<MultipartFile> attachments) {
    userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));
    channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    Message message = new Message(request.userId(), request.channelId(), request.content());
    messageRepository.save(message);

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        BinaryContent binaryContent = BinaryContent.builder()
            .id(UUID.randomUUID())
            .messageId(message.getId())
            .build();
        binaryContentRepository.save(binaryContent);
      }
    }
    return convertToResponse(message);
  }


  @Override
  public List<MessageResponse> findAllByChannelId(UUID channelId) {
    return messageRepository.findAll().stream()
        .filter(m -> m.getChannelId() != null && m.getChannelId().equals(channelId))
        .map(this::convertToResponse)
        .toList();
  }

  @Override
  public MessageResponse update(UUID id, MessageUpdateRequest request) {

    Message message = messageRepository.findById(request.id())
        .orElseThrow(() -> new NoSuchElementException("메세지를 찾을 수 없습니다."));

    message.updateContent(request.newContent());
    messageRepository.save(message);

    return convertToResponse(message);

  }

  @Override
  public void delete(UUID messageId) {

    List<BinaryContent> attachments = binaryContentRepository.findAll().stream()
        .filter(b -> messageId.equals(b.getMessageId()))
        .toList();

    for (BinaryContent binaryContent : attachments) {
      binaryContentRepository.delete(binaryContent.getId());
    }

    messageRepository.delete(messageId);
  }

  private MessageResponse convertToResponse(Message message) {
    List<UUID> attachmentIds = binaryContentRepository.findAll().stream()
        .filter(b -> message.getId().equals(b.getMessageId()))
        .map(BinaryContent::getId)
        .toList();

    return new MessageResponse(
        message.getId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getContent(),
        message.getChannelId(),
        message.getAuthorId(),
        attachmentIds
    );
  }

}
