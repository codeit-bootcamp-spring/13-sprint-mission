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
import com.sprint.mission.discodeit.exception.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
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
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Primary
@Transactional
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PageResponseMapper pageResponseMapper;
  private final ChannelMapper channelMapper;

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

        byte[] bytes;

        try {
          bytes = file.getBytes();
        } catch (IOException e) {
          throw new RuntimeException("파일 읽기 실패", e);
        }

        BinaryContent content = new BinaryContent(
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType()
        );

        binaryContentRepository.save(content);

        binaryContentStorage.put(content.getId(), bytes);

        attachmentsList.add(content);

      }
    }

    Message message = new Message(request.content(), channel, user, attachmentsList);

    messageRepository.save(message);

    return messageMapper.toDto(message);
  }

  @Transactional(readOnly = true)
  @Override
  public MessageDto findById(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new MessageNotFoundException(id));

    return messageMapper.toDto(message);
  }

  @Transactional(readOnly = true)
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable) {
    Slice<Message> slice = messageRepository.findByChannel_Id(channelId, pageable);

    List<MessageDto> messageDtos = slice.getContent().stream()
        .map(message -> messageMapper.toDto(message))
        .toList();

    Slice<MessageDto> dtoSlice = new SliceImpl<>(
        messageDtos,
        pageable,
        slice.hasNext()
    );

    return pageResponseMapper.fromSlice(dtoSlice);
  }

  @Override
  public MessageDto update(UUID id, MessageUpdateRequest request) {

    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("메세지를 찾을 수 없습니다."));

    message.updateContent(request.newContent());

    return messageMapper.toDto(message);

  }

  @Override
  public void delete(UUID messageId) {

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException("메세지를 찾을 수 없습니다."));

    List<BinaryContent> attachments = message.getAttachments();

    for (BinaryContent content : attachments) {
      binaryContentRepository.deleteById(content.getId());
    }

    messageRepository.delete(message);
  }

}
