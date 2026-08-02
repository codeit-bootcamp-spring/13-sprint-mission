package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentStorage binaryContentStorage;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public MessageDto create(MessageCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()->new UserNotFoundException(request.userId()));
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(()->new ChannelNotFoundException(request.channelId()));

        List<BinaryContent> savedFiles = new ArrayList<>();
        if(request.attachment()!=null && !request.attachment().isEmpty()) {
            for (BinaryContentCreateRequest fileDto : request.attachment()) {
                BinaryContent binaryContent = new BinaryContent(fileDto.fileName(), fileDto.contentType(), fileDto.size());
                binaryContentStorage.put(binaryContent.getId(), fileDto.bytes());
                savedFiles.add(binaryContent);
            }
        }

        Message message = new Message(user, channel, request.content(), savedFiles);
        messageRepository.save(message);

        log.info("메시지 생성 완료: id={}, channelId={}", message.getId(), channel.getId());
        return messageMapper.toDto(message);
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, Pageable pageable) {
        if (!channelRepository.existsById(channelId)) {
            throw new ChannelNotFoundException(channelId);
        }

        Instant effectiveCursor = (cursor != null) ? cursor : Instant.now();
        Slice<Message> slice = messageRepository.findAllByChannel_IdAndCreatedAtLessThan(channelId, effectiveCursor, pageable);

        List<Message> messages = slice.getContent();
        Instant nextCursor = null;
        if (slice.hasNext() && !messages.isEmpty()) {
            nextCursor = messages.get(messages.size() - 1).getCreatedAt();
        }

        Slice<MessageDto> dtoSlice = slice.map(messageMapper::toDto);
        return pageResponseMapper.fromSlice(dtoSlice, nextCursor);
    }

    @Override
    @Transactional
    public MessageDto update(MessageUpdateRequest request) {
        Message message = messageRepository.findById(request.id())
                        .orElseThrow(()->new MessageNotFoundException(request.id()));

        message.update(request.content());
        messageRepository.save(message);

        log.info("메시지 수정 완료: id={}", message.getId());
        return messageMapper.toDto(message);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(()->new MessageNotFoundException(id));

        messageRepository.delete(message);
        log.info("메시지 삭제 완료: id={}", id);
    }
}
