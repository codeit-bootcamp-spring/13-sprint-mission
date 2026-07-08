package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  // 의존성 주입
  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository contentRepository;

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
        .map(attachmentRequest -> (new BinaryContent(
            attachmentRequest.getFileName(),
            attachmentRequest.getContentType(),
            (long) attachmentRequest.getBytes().length,
            attachmentRequest.getBytes())
        )).toList(); // 아이디가 아닌 binaryContent 자체를 반환하도록 수정
    List<BinaryContent> savedAttachments = contentRepository.saveAll(attachments);
    Message message = new Message(messageCreateRequest.getContent(),
        channel, author, savedAttachments);
    Message saved = messageRepository.save(message);
    return MessageDto.from(saved);
  }

  @Override
  public MessageDto find(UUID messageId) {
    Message foundMessage = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("Message with id " + messageId + " not found"));
    return MessageDto.from(foundMessage);
  }

  @Override
  public List<MessageDto> findAllByChannelId(UUID channelId) {
    // 특정 Channel의 Message 목록 조회 조건 추가
    return messageRepository.findByChannelId(channelId).stream()
        .map(MessageDto::from)
        .toList();
  }

  @Transactional
  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("Message with id " + messageId + " not found"));
    message.update(request.getNewContent());
    return MessageDto.from(message);
  }

  @Transactional
  @Override
  public void delete(UUID messageId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("Message with id " + messageId + " not found"));
    // 관련된 도메인 BinaryContent는 CascadeType.REMOVE와 고아객체로 설정했기 때문에 같이 삭제된다
    messageRepository.delete(message);
  }
}
