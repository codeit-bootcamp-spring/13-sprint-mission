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
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  // 의존성 주입
  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository contentRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage storage;
  private final PageResponseMapper pageResponseMapper;

  @Transactional
  @Override
  public MessageDto create(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    Channel channel = channelRepository.findById(messageCreateRequest.getChannelId())
        .orElseThrow(() -> new NoSuchElementException(
            "Channel with id " + messageCreateRequest.getChannelId() + " not found"));
    User author = userRepository.findById(messageCreateRequest.getAuthorId())
        .orElseThrow(() -> new NoSuchElementException(
            "Author with id " + messageCreateRequest.getAuthorId() + " not found"));
    // 선택적으로 여러 개의 첨부파일 같이 등록 가능
    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
        .map(attachmentRequest -> {
          BinaryContent binaryContent = new BinaryContent(
              attachmentRequest.getFileName(),
              attachmentRequest.getContentType(),
              (long) attachmentRequest.getBytes().length);
          BinaryContent savedBinaryContent = contentRepository.save(binaryContent);
          storage.put(savedBinaryContent.getId(), attachmentRequest.getBytes());
          return savedBinaryContent;
        }).toList();
    Message message = new Message(messageCreateRequest.getContent(),
        channel, author, attachments);
    Message saved = messageRepository.save(message);
    log.info("메시지 생성 channelId={}, authorId={}, contentCount={}",
        messageCreateRequest.getChannelId(), messageCreateRequest.getAuthorId(),
        binaryContentCreateRequests.size());
    return messageMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public MessageDto find(UUID messageId) {
    log.debug("메시지 조회 messageId={}", messageId);
    return messageRepository.findById(messageId)
        .map(message -> messageMapper.toDto(message))
        .orElseThrow(
            () -> new NoSuchElementException("Message with id " + messageId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable) {
    // 전체 개수를 알 필요는 없기 때문에 slice
    Slice<MessageDto> slicedMessages = messageRepository.findAllByChannelId(channelId, pageable)
        .map(message -> messageMapper.toDto(message));
    log.debug("채널 목록 조회 channelId={}", channelId);
    return pageResponseMapper.fromSlice(slicedMessages);
  }

  @Transactional
  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("Message with id " + messageId + " not found"));
    message.update(request.getNewContent());
    log.info("메시지 수정 messageId={}", messageId);
    return messageMapper.toDto(message);
  }

  @Transactional
  @Override
  public void delete(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      log.warn("존재하지 않는 메시지 아이디 {}", messageId);
      throw new NoSuchElementException("Message with id " + messageId + " not found");
    }
    // 관련된 도메인 BinaryContent는 CascadeType.REMOVE와 고아객체로 설정했기 때문에 같이 삭제된다
    messageRepository.deleteById(messageId);
    log.info("메시지 삭제 messageId={}", messageId);
  }
}
