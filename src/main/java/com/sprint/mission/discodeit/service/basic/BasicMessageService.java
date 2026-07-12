package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

    private static final int PAGE_SIZE = 50;

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageMapper messageMapper;
    private final PageResponseMapper pageResponseMapper;

    @Override
    @Transactional
    public Message create(CreateMessageRequest request) {
        User author = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + request.getUserId()
                        )
                );

        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Channel not found: " + request.getChannelId()
                        )
                );

        Message message = new Message(
                request.getContent(),
                channel,
                author,
                List.of()
        );

        return messageRepository.save(message);
    }

    @Override
    public Message find(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Message not found: " + id
                        )
                );
    }

    @Override
    public PageResponse<MessageDto> findAllByChannelId(
            UUID channelId,
            int page
    ) {
        if (!channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException(
                    "Channel not found: " + channelId
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Slice<Message> messages =
                messageRepository.findAllByChannelId(
                        channelId,
                        pageable
                );

        return pageResponseMapper.fromSlice(
                messages,
                messageMapper::toDto
        );
    }

    @Override
    @Transactional
    public Message update(
            UUID id,
            UpdateMessageRequest request
    ) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Message not found: " + id
                        )
                );

        message.update(
                request.getContent(),
                message.getAttachments()
        );

        return message;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Message not found: " + id
                        )
                );

        messageRepository.delete(message);
    }
}