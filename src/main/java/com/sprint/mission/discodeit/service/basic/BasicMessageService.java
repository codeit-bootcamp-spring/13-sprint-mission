package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicMessageService implements MessageService {

  private final BinaryContentStorage binaryContentStorage;
  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;  // ← 추가
  private final PageResponseMapper pageResponseMapper; // ← 추가

  @Override
  public Message create(MessageCreateRequest messageCreateRequest,
                        List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    UUID channelId = messageCreateRequest.channelId();
    UUID authorId = messageCreateRequest.authorId();

    // UUID로 존재 여부만 체크하던 것 → 객체로 직접 로드
    Channel channel = channelRepository.findById(channelId)
            .orElseThrow(
                    () -> new NoSuchElementException("Channel with id " + channelId + " does not exist"));
    User author = userRepository.findById(authorId)
            .orElseThrow(
                    () -> new NoSuchElementException("Author with id " + authorId + " does not exist"));

    // UUID 리스트 대신 BinaryContent 객체 리스트 생성
    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
            .map(req -> {
              BinaryContent binaryContent = new BinaryContent(
                      req.fileName(),
                      (long) req.bytes().length,
                      req.contentType()
                      // bytes 파라미터 제거
              );
              BinaryContent saved = binaryContentRepository.save(binaryContent);
              // 바이너리는 Storage에 별도 저장
              binaryContentStorage.put(saved.getId(), req.bytes());
              return saved;
            })
            .toList();

    Message message = new Message(messageCreateRequest.content(), channel, author, attachments);
    return messageRepository.save(message);
  }

  @Transactional(readOnly = true)
  @Override
  public Message find(UUID messageId) {
    return messageRepository.findById(messageId)
            .orElseThrow(
                    () -> new NoSuchElementException("Message with id " + messageId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<Message> findAllByChannelId(UUID channelId) {
    return messageRepository.findAllByChannel_Id(channelId).stream().toList();
  }

  @Override
  public Message update(UUID messageId, String newContent) {
    Message message = messageRepository.findById(messageId)
            .orElseThrow(
                    () -> new NoSuchElementException("Message with id " + messageId + " not found"));
    message.update(newContent);
    return messageRepository.save(message);
  }

  @Override
  public void delete(UUID messageId) {
    if (!messageRepository.existsById(messageId)) {
      throw new NoSuchElementException("Message with id " + messageId + " not found");
    }
    // cascade로 attachments(BinaryContent) 자동 삭제
    messageRepository.deleteById(messageId);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<Message> findAllByChannelId(UUID channelId, Pageable pageable) {
    Slice<Message> slice = messageRepository.findAllByChannel_Id(channelId, pageable);
    return pageResponseMapper.fromSlice(slice);
  }
}