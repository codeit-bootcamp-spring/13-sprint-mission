package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicMessageService implements MessageService {

    private static final int MESSAGE_PAGE_SIZE = 50;

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;

    @Override
    public MessageResponse create(MessageCreateRequest dto) {
        int attachmentCount = dto.binaryContentIds() == null ? 0 : dto.binaryContentIds().size();
        log.debug("메시지 생성 시작: channelId={}, senderId={}, attachmentCount={}",
                dto.channelId(), dto.senderId(), attachmentCount);
        Channel channel = channelRepository.findById(dto.channelId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
        User author = userRepository.findById(dto.senderId())
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        Message message = new Message(dto.content(), channel, author);

        if (dto.binaryContentIds() != null && !dto.binaryContentIds().isEmpty()) {
            List<BinaryContent> attachments = binaryContentRepository.findAllById(dto.binaryContentIds());
            message.getAttachments().addAll(attachments);
        }

        Message savedMessage = messageRepository.save(message);
        log.info("메시지 생성 완료: messageId={}, channelId={}",
                savedMessage.getId(), dto.channelId());
        return messageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MessageResponse> findById(UUID id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findAll(UUID channelId) {
        return findAllByChannelId(channelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId).stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> findAllByChannelId(UUID channelId, int page) {
        Pageable pageable = PageRequest.of(
                page,
                MESSAGE_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Slice<MessageResponse> messages = messageRepository
                .findAllByChannelId(channelId, pageable)
                .map(messageMapper::toDto);

        return pageResponseMapper.toDto(messages);
    }

    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest dto) {
        log.debug("메시지 수정 시작: messageId={}", id);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메시지를 찾을 수 없습니다."));

        message.updateContent(dto.content());

        log.info("메시지 수정 완료: messageId={}", id);
        return messageMapper.toDto(message);
    }

    @Override
    public void delete(UUID id) {
        log.debug("메시지 삭제 시작: messageId={}", id);
        messageRepository.deleteById(id);
        log.info("메시지 삭제 완료: messageId={}", id);
    }
}
