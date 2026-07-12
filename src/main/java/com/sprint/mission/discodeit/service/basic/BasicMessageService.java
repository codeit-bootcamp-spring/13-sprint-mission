package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
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
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request) {
    log.info("메시지 생성 요청 - channelId: {}, authorId: {}"
        , request.channelId(), request.authorId());

    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 user입니다."));

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 channelId입니다."));

    Message message = new Message(request.content(), user, channel);

    List<BinaryContent> attachments = binaryContentRepository.findAllByIdIn(
        request.attachmentIds());
    message.updateAttachmentIds(attachments);

    messageRepository.save(message);

    log.info("메시지 생성 완료 - messageId: {}", message.getId());
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional(readOnly = true)
  public MessageDto find(UUID messageId) {
    log.info("메시지 단건 조회 요청 - messageId: {}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("존재하지 않는 messageId 입니다."));

    log.info("메시지 단건 조회 완료- messageId: {}", messageId);
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor) {
    log.info("채널 메시지 목록 조회 요청 - channelId: {}", channelId);

    PageRequest pageable = PageRequest.of(0, 50,
        Sort.by(Direction.DESC, "createdAt"));

    Slice<Message> slice;

    if (cursor != null) {
      slice = messageRepository.findAllByChannel_IdAndCreatedAtBefore(
          channelId, cursor, pageable);
    } else {
      slice = messageRepository.findAllByChannel_Id(channelId, pageable);
    }

    Slice<MessageDto> dtoSlice = slice.map(messageMapper::toDto);

    Instant nextCursor = dtoSlice.hasNext()
        ? dtoSlice.getContent().get(dtoSlice.getContent().size() - 1).createdAt()
        : null;

    log.info("채널 메시지 목록 조회 완료 - 페이지: {}, 다음 페이지 여부: {}",
        dtoSlice.getNumber(), dtoSlice.hasNext());
    return pageResponseMapper.fromSlice(dtoSlice, nextCursor);
  }

  @Override
  @Transactional
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    log.info("메시지 수정 요청 - messageId: {}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("존재하지 않는 messageId 입니다."));
    message.updateMessage(request.newContent());

    log.info("메시지 수정 완료 - messageId: {}", messageId);
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void delete(UUID messageId) {
    log.info("메시지 삭제 요청 - messageId: {}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(
            () -> new NoSuchElementException("존재하지 않는 messageId 입니다."));
    messageRepository.deleteById(messageId);

    log.info("메시지 삭제 완료 - messageId: {}", messageId);
  }
}