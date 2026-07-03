package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
  public MessageDto create(MessageCreateRequest request, List<MultipartFile> attachments) {
    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> new UserNotFoundException(request.authorId()));
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new ChannelNotFoundException(request.channelId()));

    List<BinaryContent> attachmentsList = new ArrayList<>();

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        if (file.isEmpty()) {
          continue;
        }

        try {
          BinaryContent content = new BinaryContent(
              file.getOriginalFilename(),
              file.getSize(),
              file.getContentType(),
              file.getBytes()
          );

          binaryContentRepository.save(content);
          attachmentsList.add(content);

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    Message message = new Message(request.content(), channel, user, attachmentsList);

    messageRepository.save(message);

    return convertToResponse(message);
  }


  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable) {
    throw new UnsupportedOperationException("페이지네이션 구현 예정");
  }

  @Override
  public MessageDto update(UUID id, MessageUpdateRequest request) {

    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("메세지를 찾을 수 없습니다."));

    message.updateContent(request.newContent());
    messageRepository.save(message);

    return convertToResponse(message);

  }

  @Override
  public void delete(UUID messageId) {

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException("메세지를 찾을 수 없습니다."));

    List<BinaryContent> attachments = message.getAttachments();

    for (BinaryContent content : attachments) {
      binaryContentRepository.deleteById(content.getId());
    }

    messageRepository.deleteById(messageId);
  }

  private MessageDto convertToResponse(Message message) {
    List<UUID> attachmentIds = message.getAttachments().stream()
        .map(BinaryContent::getId)
        .toList();

    return new MessageDto(
        message.getId(),
        message.getCreatedAt(),
        message.getUpdatedAt(),
        message.getContent(),
        message.getChannel().getId(),
        message.getAuthor().getId(),
        attachmentIds
    );
  }

}
