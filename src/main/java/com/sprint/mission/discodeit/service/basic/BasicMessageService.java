package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import com.sprint.mission.discodeit.exception.channel.ChannelAccessDeniedException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository repository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  public boolean isPrivateChannelContainsAuthor(MessageCreateRequest request, Channel channel) {
    if (channel.getType() == ChannelType.PRIVATE) {

      Channel repoChannel = channelRepository.findById(channel.getId())
          .orElseThrow(() -> new ChannelNotFoundException(channel.getId()));
      return repoChannel.getAllowedUserList().contains(request.authorId());
    }
    return true;
  }

  @Override
  @Transactional
  public MessageResponse createMessage(MessageCreateRequest request,
      List<MultipartFile> attachments) {
    int attachmentCount = attachments == null ? 0 : attachments.size();
    log.info("메시지 생성 시작: channelId={}, authorId={}, attachmentCount={}",
        request.channelId(), request.authorId(), attachmentCount);
    Channel channel = getChannelOrThrow(request.channelId());
    User user = getUserOrThrow(request.authorId());
    if (!isAccessable(request, user, channel)) {
      throw new ChannelAccessDeniedException(channel.getId(), user.getId());
    }
    Message message = request.toEntity(user, channel);

    if (attachments != null && !attachments.isEmpty()) {
      message.updateAttachment(saveAttachment(message, attachments));
    } else {
      log.info("메시지에 첨부파일이 없습니다.");
    }

    repository.save(message);
    log.info("메시지 생성 - {}", message.getContent());
    return messageMapper.toDto(message);
  }

  @Override
  @Transactional(readOnly = true)
  public MessageResponse findMessageById(UUID messageId) {
    Message message = getMessageOrThrow(messageId);
    MessageResponse response = messageMapper.toDto(message);
    log.info("메시지 조회 - {}", message.getContent());
    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<MessageResponse> findAllMessageByChannelId(
      UUID channelId, Instant cursor, org.springframework.data.domain.Pageable pageable) {
    System.out.println("메세지 전체조회 -");
    getChannelOrThrow(channelId);

    var cursorPageable = PageRequest.of(
        0,
        pageable.getPageSize(),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    var messageSlice = cursor == null
        ? repository.findAllByChannelId(channelId, cursorPageable)
        : repository.findAllByChannelIdAndCreatedAtLessThan(channelId, cursor, cursorPageable);

    Object nextCursor = null;
    if (messageSlice.hasNext() && !messageSlice.isEmpty()) {
      Message lastMessage = messageSlice.getContent().get(messageSlice.getNumberOfElements() - 1);
      nextCursor = lastMessage.getCreatedAt();
    }

    return pageResponseMapper.fromSlice(
        messageSlice.map(messageMapper::toDto),
        nextCursor,
        repository.countByChannelId(channelId));
  }

  @Override
  @Transactional
  @PreAuthorize("@resourceOwnership.isMessageAuthor(#messageId, principal)")
  public MessageResponse updateMessage(UUID messageId, MessageUpdateRequest request) {
    log.info("메시지 수정 시작: messageId={}", messageId);
    Message message = getMessageOrThrow(messageId);

    message.updateContent(request.newContent());

    repository.save(message);
    log.info("{} 메시지 수정됨", message.getContent());

    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  @PreAuthorize("@resourceOwnership.isMessageAuthor(#messageId, principal)")
  public void deleteMessage(UUID messageId) {
    log.info("메시지 삭제 시작: messageId={}", messageId);
    Message message = getMessageOrThrow(messageId);
    if (message.getAttachment() != null && !message.getAttachment().isEmpty()) {
      List<BinaryContent> attachments = message.getAttachment();
      attachments.removeIf(binaryContent -> binaryContent.getId() != null);
      log.info("첨부파일 삭제");
    }
    repository.deleteById(messageId);
    log.info("메시지 삭제 완료: messageId={}", messageId);
  }

  private Message getMessageOrThrow(UUID messageId) {
    return repository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException(messageId));
  }

  private Channel getChannelOrThrow(UUID channelId) {
    return channelRepository.findById(channelId)
        .orElseThrow(() -> new ChannelNotFoundException(channelId));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("userId", userId));
  }

  private boolean isAccessable(MessageCreateRequest request, User user, Channel channel) {
    if (channel.getType() == ChannelType.PRIVATE) {
      if (!isPrivateChannelContainsAuthor(request, channel)) {
        System.out.println("접근 제한 채널 - 생성할 수 없습니다.");
        return false;
      }
    } else if (channel.getType() == ChannelType.MANAGER) {
      if (user.getRole() != Role.CHANNEL_MANAGER) {
        System.out.println("매니저 전용 채널 - 생성할 수 없습니다.");
        return false;
      }
    }
    return true;
  }

  private List<BinaryContent> saveAttachment(Message message, List<MultipartFile> attachments) {
    if (attachments == null || attachments.isEmpty()) {
      return List.of();
    }

    return attachments.stream()
        .filter(file -> file != null && !file.isEmpty())
        .map(file -> saveBinaryContent(message, file))
        .toList();
  }

  private BinaryContent saveBinaryContent(Message message, MultipartFile file) {
    String savedFileName = file.getOriginalFilename();
    String contentType = file.getContentType();
    Long size = file.getSize();

    BinaryContent binaryContent = new BinaryContent(
        savedFileName,
        size,
        contentType,
        null,
        message
    );

    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
    log.info("파일 업로드 시작: binaryContentId={}, messageId={}, size={}, contentType={}",
        savedBinaryContent.getId(), message.getId(), size, contentType);

    try {
      byte[] bytes = file.getBytes();
      binaryContentStorage.put(savedBinaryContent.getId(), bytes);
    } catch (IOException e) {
      throw new BinaryContentStorageException(savedFileName, e);
    }

    log.info("파일 {} 저장됨", savedFileName);

    log.info("파일 업로드 완료: binaryContentId={}, messageId={}",
        savedBinaryContent.getId(), message.getId());
    return savedBinaryContent;
  }
}
