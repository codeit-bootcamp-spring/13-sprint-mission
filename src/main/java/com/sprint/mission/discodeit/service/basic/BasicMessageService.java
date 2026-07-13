package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository contentRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;

    @Override
    public MessageDto create(MessageCreateRequest request) {
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new NoSuchElementException("채널을 찾을 수 없습니다. ID: " + request.channelId()));
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new NoSuchElementException("저자를 찾을 수 없습니다. ID: " + request.authorId()));

        List<BinaryContent> attachments = request.attachments() == null ? List.of()
                : request.attachments().stream()
                .map(attach -> {
                    BinaryContent saved = contentRepository.save(BinaryContent.create(
                            attach.fileName(),
                            attach.size(),
                            attach.contentType()));
                    binaryContentStorage.put(saved.getId(), attach.bytes());
                    return saved;
                })
                .toList();

        Message message = Message.create(request.content(), channel, author, attachments);
        Message savedMessage = messageRepository.save(message);

        log.info("메시지: {}가 생성됨.", savedMessage.getId());
        return messageMapper.toDto(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageDto find(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("메시지 ID: " + messageId + " 를 찾을 수 없습니다."));

        return messageMapper.toDto(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널 ID: " + channelId + " 를 찾을 수 없습니다.");
        }

        return messageRepository.findAllByChannelId(channelId).stream()
                .map(messageMapper::toDto)
                .toList();
    }

    @Override
    public MessageDto update(MessageUpdateRequest request) {
        Message message = messageRepository.findById(request.messageId())
                .orElseThrow(() -> new NoSuchElementException("메시지 ID: " + request.messageId() + " 를 찾을 수 없습니다."));

        message.update(request.newContent());

        log.info("메시지: {}가 수정됨.", message.getId());
        return messageMapper.toDto(message);
    }

    @Override
    public void delete(UUID messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new NoSuchElementException("메시지 ID: " + messageId + " 를 찾을 수 없습니다.");
        }

        messageRepository.deleteById(messageId);
        log.info("메시지: {}가 삭제됨.", messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new NoSuchElementException("채널 ID: " + channelId + " 를 찾을 수 없습니다.");
        }

        Slice<MessageDto> messages = messageRepository.findAllByChannelId(channelId, pageable)
                .map(messageMapper::toDto);

        return pageResponseMapper.fromSlice(messages);
    }
}