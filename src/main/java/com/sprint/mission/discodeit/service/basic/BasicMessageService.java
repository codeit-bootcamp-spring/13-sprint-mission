package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PageResponseMapper pageResponseMapper;

  @Override
  public MessageDto create(
      MessageCreateRequest request,
      List<MultipartFile> attachments
  ) {
    int attachmentCount = attachments == null ? 0 : attachments.size();

    log.debug(
        "메시지 생성 시작: authorId={}, channelId={}, attachmentCount={}",
        request.authorId(),
        request.channelId(),
        attachmentCount
    );

    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> {
          log.warn(
              "메시지 생성 실패: 작성자를 찾을 수 없음, authorId={}",
              request.authorId()
          );
          return new UserNotFoundException(request.authorId());
        });

    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> {
          log.warn(
              "메시지 생성 실패: 채널을 찾을 수 없음, channelId={}",
              request.channelId()
          );
          return new ChannelNotFoundException(request.channelId());
        });

    List<BinaryContent> attachmentsList = new ArrayList<>();

    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        if (file.isEmpty()) {
          log.debug("빈 첨부파일 건너뜀");
          continue;
        }

        log.debug(
            "메시지 첨부파일 업로드 시작: fileName={}, size={}",
            file.getOriginalFilename(),
            file.getSize()
        );

        byte[] bytes;

        try {
          bytes = file.getBytes();
        } catch (IOException e) {
          log.error(
              "메시지 첨부파일 읽기 실패: fileName={}",
              file.getOriginalFilename(),
              e
          );

          throw new BinaryContentUploadException(
              file.getOriginalFilename(),
              e
          );
        }

        BinaryContent content = new BinaryContent(
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType()
        );

        binaryContentRepository.save(content);
        binaryContentStorage.put(content.getId(), bytes);
        attachmentsList.add(content);

        log.debug(
            "메시지 첨부파일 업로드 완료: binaryContentId={}",
            content.getId()
        );
      }
    }

    Message message = new Message(
        request.content(),
        channel,
        user,
        attachmentsList
    );

    messageRepository.save(message);

    log.info(
        "메시지 생성 완료: messageId={}, channelId={}, attachmentCount={}",
        message.getId(),
        channel.getId(),
        attachmentsList.size()
    );

    return messageMapper.toDto(message);
  }

  @Transactional(readOnly = true)
  @Override
  public MessageDto findById(UUID messageId) {
    log.debug("메시지 단건 조회 시작: messageId={}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn(
              "메시지 조회 실패: messageId={}를 찾을 수 없음",
              messageId
          );
          return new MessageNotFoundException(messageId);
        });

    log.debug("메시지 단건 조회 완료: messageId={}", messageId);

    return messageMapper.toDto(message);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<MessageDto> findAllByChannelId(
      UUID channelId,
      Pageable pageable
  ) {
    log.debug(
        "채널별 메시지 조회 시작: channelId={}, page={}, size={}",
        channelId,
        pageable.getPageNumber(),
        pageable.getPageSize()
    );

    Pageable fixedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Direction.DESC, "createdAt")
    );

    Slice<Message> slice =
        messageRepository.findByChannel_Id(channelId, fixedPageable);

    List<MessageDto> messageDtos = slice.getContent().stream()
        .map(messageMapper::toDto)
        .toList();

    Slice<MessageDto> dtoSlice = new SliceImpl<>(
        messageDtos,
        pageable,
        slice.hasNext()
    );

    log.debug(
        "채널별 메시지 조회 완료: channelId={}, count={}, hasNext={}",
        channelId,
        messageDtos.size(),
        slice.hasNext()
    );

    return pageResponseMapper.fromSlice(dtoSlice);
  }

  @Override
  public MessageDto update(
      UUID messageId,
      MessageUpdateRequest request
  ) {
    log.debug("메시지 수정 시작: messageId={}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn(
              "메시지 수정 실패: messageId={}를 찾을 수 없음",
              messageId
          );
          return new MessageNotFoundException(messageId);
        });

    message.updateContent(request.newContent());

    log.info("메시지 수정 완료: messageId={}", messageId);

    return messageMapper.toDto(message);
  }

  @Override
  public void delete(UUID messageId) {
    log.debug("메시지 삭제 시작: messageId={}", messageId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn(
              "메시지 삭제 실패: messageId={}를 찾을 수 없음",
              messageId
          );
          return new MessageNotFoundException(messageId);
        });

    List<BinaryContent> attachments = message.getAttachments();

    if (attachments != null) {
      log.debug(
          "메시지 첨부파일 삭제 시작: messageId={}, attachmentCount={}",
          messageId,
          attachments.size()
      );

      for (BinaryContent content : attachments) {
        UUID attachmentId = content.getId();

        binaryContentStorage.delete(attachmentId);
        binaryContentRepository.deleteById(attachmentId);
      }
    }

    messageRepository.delete(message);

    log.info("메시지 삭제 완료: messageId={}", messageId);
  }

}
